use crate::common::Rslt;
use libc::{closedir, dirent, opendir, readdir, DT_DIR};
use std::ffi::{CStr, CString};
use std::io;

type ErrCount = u32;
type LibcInt = libc::c_int;
type StatInt = libc::mode_t;

const OK: LibcInt = 0;

/*#[cfg(all(target_os = "android", target_arch = "x86"))]
const SYS_UNLINKAT2: c_int = 453;
#[cfg(all(target_os = "android", target_arch = "x86_64"))]
const SYS_UNLINKAT2: libc::c_long = 437;
#[cfg(all(target_os = "android", target_arch = "arm"))]
const SYS_UNLINKAT2: c_int = 451;
#[cfg(all(target_os = "android", target_arch = "aarch64"))]
const SYS_UNLINKAT2: libc::c_long = 451;
#[cfg(any(target_os = "macos", target_os = "windows"))]
const SYS_UNLINKAT2: c_int = 281;
const AT_RECURSIVE: c_int = 0x200;*/

const CUR_DIR: &[u8; 1] = b".";
const PARENT_DIR: &[u8; 2] = b"..";

pub fn delete(path: &String) -> Rslt<ErrCount> {
    let c_path = CString::new(path.as_str())?;
    let st_dev = get_dev(&c_path);
    return match delete_recursively(&c_path, false, st_dev) {
        Ok(0) => Ok(0),
        Ok(_) => delete_recursively(&c_path, false, st_dev),
        Err(e) => return Err(e),
    };
}

pub fn delete_recursively(path: &CString, as_dir: bool, st_dev: StatInt) -> Rslt<ErrCount> {
    if get_dev(&path) != st_dev || call_delete(&path, as_dir) == OK {
        return Ok(0);
    }
    let error = io::Error::last_os_error()
        .raw_os_error()
        .unwrap_or(0);
    return if error == libc::ENOENT {
        Ok(0) //             vvvvvv - no AT_REMOVEDIR  vvvvvvvvv - no AT_RECURSIVE
    } else if error == libc::EISDIR {
        delete_recursively(path, true, st_dev)
    } else if error == libc::ENOTDIR {
        delete_recursively(path, false, st_dev)
    } else if error == libc::ENOTEMPTY {
        delete_children(path, st_dev)
    } else if error == libc::EPERM || error == libc::EBUSY { // retry
        Ok(0)
    /* SYS_UNLINKAT2 => Fatal signal 31 (SIGSYS), code 1 (SYS_SECCOMP), syscall 451
    } else if result == libc::ENOSYS { // fallback
        delete_recursively(path, st_dev, as_dir, true)*/
    } else {
        Err(io::Error::last_os_error().into())
    };
}

pub fn delete_children(path: &CString, st_dev: StatInt) -> Rslt<ErrCount> {
    unsafe {
        let dir = opendir(path.as_ptr());
        if dir.is_null() {
            return Err(io::Error::last_os_error().into())
        }
        let mut err_count: ErrCount = 0;
        loop {
            let entry = readdir(dir);
            if entry.is_null() {
                break;
            }
            let name = CStr::from_ptr((*entry).d_name.as_ptr())
                .to_bytes();
            if name == CUR_DIR || name == PARENT_DIR {
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
            err_count += match CString::from_vec_with_nul(buf) {
                Ok(child_path) => delete_recursively(&child_path, is_dir, st_dev).unwrap_or(1),
                Err(_) => 1
            };
        }
        closedir(dir);
        return Ok(err_count);
    }
}

fn call_delete(path: &CString, as_dir: bool) -> LibcInt {
    let flags = match as_dir {
        true => libc::AT_REMOVEDIR,
        false => 0,
    };
    unsafe {
        let c_path = path.as_ptr() as *const libc::c_char;
        libc::unlinkat(libc::AT_FDCWD, c_path, flags) as LibcInt
    }
}

fn get_dev(path: &CString) -> StatInt {
    let mut stat: libc::stat = unsafe { std::mem::zeroed() };
    let result = unsafe {
        libc::stat(path.as_ptr(), &mut stat)
    };
    return match result {
        0 => stat.st_mode as StatInt,
        _ => 0,
    }
}
