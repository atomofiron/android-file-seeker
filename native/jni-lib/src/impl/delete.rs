use crate::api::protocol::{ComplexResult, ProgressCollector};
use crate::common::{Rslt, OKI};
use crate::ext::result::ResultExt;
use crate::r#impl::progress::{convert_progress, send_inc, ProgressChange};
use libc::{c_int, closedir, dirent, mode_t, opendir, readdir, DT_DIR};
use std::ffi::{CStr, CString};
use std::fmt::Display;
use std::io;
use std::ops::Range;
use std::os::unix::ffi::OsStrExt;
use std::path::PathBuf;
use std::sync::mpsc::{channel, Sender};
use std::sync::Arc;

/*#[cfg(all(target_os = "android", target_arch = "x86"))]
const SYS_UNLINKAT2: c_int = 453;
#[cfg(all(target_os = "android", target_arch = "x86_64"))]
const SYS_UNLINKAT2: libc::c_long = 437;
#[cfg(all(target_os = "android", target_arch = "arm"))]
const SYS_UNLINKAT2: c_int = 451;
#[cfg(all(target_os = "android", target_arch = "aarch64"))]
const SYS_UNLINKAT2: libc::c_long = 451;

const AT_RECURSIVE: c_int = 0x200;*/

const CURRENT_DIR: &[u8; 1] = b".";
const PARENT_DIR: &[u8; 2] = b"..";

pub fn delete_impl(path: &PathBuf, collector: Arc<dyn ProgressCollector>) -> Rslt<ComplexResult> {
    let (tx, rx) = channel::<ProgressChange>();
    let handle = convert_progress(rx, collector, None);
    let result = delete(path, &tx, 0.0..1.0);
    drop(tx);
    result?;
    return handle.join().map_err(|_| "Joining thread failed".into());
}

pub fn delete(path: &PathBuf, tx: &Sender<ProgressChange>, range: Range<f32>) -> Rslt<()> {
    let c_path = CString::new(path.as_os_str().as_bytes())?;
    let st_dev = get_dev(&c_path)?;
    delete_recursively(&c_path, false, st_dev, tx, range)?;
    match path.exists() { // 1 retry
        true => delete_recursively(&c_path, false, st_dev, tx, 1.0..1.0),
        _ => Ok(()),
    }
}

pub fn delete_recursively(
    path: &CString,
    as_dir: bool,
    st_dev: mode_t,
    tx: &Sender<ProgressChange>,
    range: Range<f32>,
) -> Rslt<()> {
    match get_dev(path) {
        Ok(stat) if stat == st_dev => (),
        Ok(_) => return Ok(()),
        Err(e) => return send_err(path, e, tx, &range),
    }
    match call_delete(path, as_dir) {
        Ok(OKI) => return Ok(()),
        Ok(_) => (), // os error
        Err(e) => return send_err(path, e, tx, &range),
    }
    let error = io::Error::last_os_error()
        .raw_os_error()
        .unwrap_or(0);
    return if error == libc::ENOENT {
        Ok(()) //            vvvvvv - no AT_REMOVEDIR  vvvvvvvvv - no AT_RECURSIVE
    } else if error == libc::EISDIR {
        delete_recursively(path, true, st_dev, tx, range)
    } else if error == libc::ENOTDIR {
        delete_recursively(path, false, st_dev, tx, range)
    } else if error == libc::ENOTEMPTY {
        delete_children(path, st_dev, tx, range)
    } else if error == libc::EPERM || error == libc::EBUSY { // retry
        Ok(())
    /* SYS_UNLINKAT2 => Fatal signal 31 (SIGSYS), code 1 (SYS_SECCOMP), syscall 451
    } else if result == libc::ENOSYS { // fallback
        delete_recursively(path, st_dev, as_dir, true)*/
    } else {
        send_err(path, io::Error::last_os_error(), tx, &range)
    };
}

pub fn delete_children(
    path: &CString,
    st_dev: mode_t,
    tx: &Sender<ProgressChange>,
    range: Range<f32>,
) -> Rslt<()> {
    unsafe {
        let dir = opendir(path.as_ptr());
        if dir.is_null() {
            return send_err(path, io::Error::last_os_error(), tx, &range);
        }
        let child_count = match child_count(path) {
            Ok(count) => count,
            Err(e) => return send_err(path, e, tx, &range),
        };
        let step = (range.end - range.start) / child_count as f32;
        let i = 0f32;
        loop {
            let entry = readdir(dir);
            if entry.is_null() {
                break;
            }
            let name = CStr::from_ptr((*entry).d_name.as_ptr())
                .to_bytes();
            if name == CURRENT_DIR || name == PARENT_DIR {
                continue;
            }
            let parent = path.to_bytes();
            let mut buf = Vec::with_capacity(parent.len() + 1 + name.len() + 1);
            buf.extend_from_slice(parent);
            buf.push(b'/');
            buf.extend_from_slice(name);
            buf.push(0); // end
            let dent: &dirent = &*entry;
            let is_dir = dent.d_type == DT_DIR;
            let range_start = range.start;
            let result = CString::from_vec_with_nul(buf).boxed().and_then(|it| {
                let offset = step * i;
                let start = range_start + offset;
                let range = start..(start + step);
                delete_recursively(&it, is_dir, st_dev, tx, range)
            });
            match result {
                Ok(_) => send_inc(tx, &range)?,
                Err(e) => send_err(path, e, tx, &range)?,
            }
        }
        let _ = closedir(dir);
        return Ok(());
    }
}

pub fn child_count(path: &CString) -> Rslt<u32> {
    unsafe {
        let dir = opendir(path.as_ptr());
        if dir.is_null() {
            return Err(io::Error::last_os_error().into())
        }
        let mut count = 0u32;
        loop {
            let entry = readdir(dir);
            if entry.is_null() {
                break;
            }
            count += 1;
        }
        let _ = closedir(dir);
        return Ok(count);
    }
}

fn call_delete(path: &CString, as_dir: bool) -> Rslt<c_int> {
    let flags = match as_dir {
        true => libc::AT_REMOVEDIR,
        false => 0,
    };
    unsafe {
        let c_path: *const libc::c_char = path.as_ptr().try_into()?;
        let code: c_int = libc::unlinkat(libc::AT_FDCWD, c_path, flags).try_into()?;
        return Ok(code)
    }
}

fn get_dev(path: &CString) -> Rslt<mode_t> {
    let mut stat: libc::stat = unsafe { std::mem::zeroed() };
    let result = unsafe {
        libc::stat(path.as_ptr(), &mut stat)
    };
    return match result {
        OKI => stat.st_mode.try_into().boxed(),
        _ => Err(io::Error::last_os_error().into()),
    }
}

pub fn send_err(
    path: &CString,
    error: impl Display,
    tx: &Sender<ProgressChange>,
    range: &Range<f32>,
) -> Rslt<()> {
    let path = path.clone().into_bytes();
    let change = ProgressChange::Err(path, error.to_string(), range.end);
    return tx.send(change).boxed();
}
