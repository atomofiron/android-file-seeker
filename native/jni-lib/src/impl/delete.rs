// Assisted-by: Sonnet 4.5

use crate::api::protocol::{CommonProgressCollector, CountingResult};
use crate::common::{Rslt, OKI, PERMISSION_DENIED, RESOURCE_BUSY};
use crate::ext::result::ResultExt;
use crate::r#impl::progress::{convert_progress, send_inc, ProgressChange};
use libc::{c_int, c_uint, closedir, dev_t, mode_t, opendir, readdir};
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

pub fn delete_impl(path: &PathBuf, collector: Arc<dyn CommonProgressCollector>) -> Rslt<CountingResult> {
    let (tx, rx) = channel::<ProgressChange>();
    let handle = convert_progress(rx, collector, path.clone());
    let result = delete(path, &tx, 0.0..1.0);
    drop(tx);
    result?;
    return handle.join().map_err(|_| "Joining thread failed".into());
}

pub fn delete(path: &PathBuf, tx: &Sender<ProgressChange>, range: Range<f32>) -> Rslt<()> {
    let c_path = CString::new(path.as_os_str().as_bytes())?;
    let (dev, _) = get_dev_mode(&c_path)?;
    send_err(&c_path, format!("target {dev}"), tx, &range)?;
    delete_recursively(&c_path, dev, tx, range, true)?;
    match path.exists() { // 1 retry
        true => delete_recursively(&c_path, dev, tx, 1.0..1.0, false),
        _ => Ok(()),
    }
}

pub fn delete_recursively(
    path: &CString,
    root_dev: dev_t,
    tx: &Sender<ProgressChange>,
    range: Range<f32>,
    first_try: bool,
) -> Rslt<()> {
    let mode = match get_dev_mode(path) {
        //Ok((dev, mode)) if dev == root_dev => mode,
        /*Ok((dev, _)) => {
            send_err(&path, format!("target {dev}"), tx, &range)?;
            return Ok(())
        },*/
        Ok((_, mode)) => mode,
        Err(e) => return send_err(path, e, tx, &range),
    };
    let as_dir = (mode as mode_t & libc::S_IFMT) == libc::S_IFDIR;
    match call_delete(path, as_dir) {
        Ok(OKI) => return Ok(()),
        Ok(_) => (), // os error
        Err(e) => return send_err(path, e, tx, &range),
    }
    let error = io::Error::last_os_error()
        .raw_os_error()
        .unwrap_or(0);
    return if error == libc::ENOENT {
        Ok(()) //            vvvvvvvvv - no AT_RECURSIVE
    } else if error == libc::ENOTEMPTY {
        delete_children(path, root_dev, tx, range, first_try)
    } else if error == libc::EPERM { // retry
        if !first_try {
            send_err(path, PERMISSION_DENIED, tx, &range)?;
        }
        Ok(())
    } else if error == libc::EBUSY { // retry
        if !first_try {
            send_err(path, RESOURCE_BUSY, tx, &range)?;
        }
        Ok(())
    } else {
        send_err(path, io::Error::last_os_error(), tx, &range)
    };
}

pub fn delete_children(
    path: &CString,
    root_dev: dev_t,
    tx: &Sender<ProgressChange>,
    range: Range<f32>,
    first_try: bool,
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
            let range_start = range.start;
            let result = CString::from_vec_with_nul(buf).boxed().and_then(|it| {
                let offset = step * i;
                let start = range_start + offset;
                let range = start..(start + step);
                delete_recursively(&it, root_dev, tx, range, first_try)
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

fn get_dev_mode(path: &CString) -> Rslt<(dev_t, c_uint)> {
    let mut stat: libc::stat = unsafe { std::mem::zeroed() };
    let result = unsafe {
        libc::lstat(path.as_ptr(), &mut stat)
    };
    return match result {
        OKI => Ok((stat.st_dev as dev_t, stat.st_mode)),
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
