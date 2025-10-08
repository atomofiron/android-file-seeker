// with the support of Sonnet 4.5

use crate::api::protocol::{Progress, ProgressCollector};
use crate::common::{Rslt, OKI};
use crate::ext::raw_path::RawPath;
use crate::r#impl::copy_method::CopyMethod;
use crate::r#impl::delete::delete;
use crate::r#impl::progress::ProgressChange;
use libc::off_t;
use std::ffi::CString;
use std::fs::{self, DirEntry, File, Metadata};
use std::io::{self, Read, Write};
use std::ops::Range;
use std::os::unix::ffi::OsStrExt;
use std::os::unix::fs::{MetadataExt, PermissionsExt};
use std::os::unix::io::AsRawFd;
use std::path::{Path, PathBuf};
use std::sync::mpsc::{channel, Sender};
use std::sync::Arc;

const BYTES_LEFT: &str = "bytes left";
const INVALID_PATH: &str = "Invalid path";

const MAX_CHUNK: usize = 64 * 1024 * 1024;
const BUFFER_SIZE: usize = 1024 * 1024;

pub fn copy_impl(
    from: PathBuf,
    to: PathBuf,
    moving: bool,
    collector: Arc<dyn ProgressCollector>,
) -> Rslt<()> {
    let method = CopyMethod::detect();
    let (tx, rx) = channel::<ProgressChange>();
    let handle = std::thread::spawn(move || {
        let mut progress = Progress::new();
        collector.invoke(progress.clone());
        let mut errors: Vec<RawPath> = Vec::new();
        loop {
            match rx.recv() {
                Err(_) => break,
                Ok(ProgressChange::Increment(new)) => progress.inc(new),
                Ok(ProgressChange::Update(new)) => progress.update(new),
                Ok(ProgressChange::Err(path, new)) => {
                    progress.inc_error(new);
                    errors.push(path)
                },
            }
            collector.invoke(progress.clone());
        }
    });
    if moving {
        match fs::rename(&from, &to) {
            Ok(()) => (),
            Err(e) if e.raw_os_error() == Some(libc::EXDEV) => {
                copy_recursive(&from, &to, method, &tx, 0.0..0.5)?;
                delete(&from, &tx, 0.5..1.0)?;
            }
            Err(e) => Err(e)?,
        }
    } else {
        copy_recursive(&from, &to, method, &tx, 0.0..1.0)?;
    }
    drop(tx);
    handle.join().unwrap_or(());
    return Ok(());
}

fn copy_recursive(
    from: &Path,
    to: &Path,
    method: CopyMethod,
    tx: &Sender<ProgressChange>,
    range: Range<f32>,
) -> Rslt<()> {
    let metadata = fs::symlink_metadata(from)?;
    if metadata.is_symlink() {
        let target = fs::read_link(from)?;
        std::os::unix::fs::symlink(target, to)?;
        copy_symlink_metadata(&metadata, to)?;
        tx.send(ProgressChange::Increment(range.end))?;
    } else if metadata.is_dir() {
        fs::create_dir_all(to)?;

        let children: Vec<DirEntry> = fs::read_dir(from)
            ?.collect::<Result<_, _>>()?;
        let child_count = children.len();
        let step = (range.end - range.start) / child_count as f32;
        for (i, entry) in children.into_iter().enumerate() {
            let from_path = entry.path();
            let to_path = to.join(entry.file_name());

            let offset = step * i as f32;
            let start = range.start + offset;
            let range = start..(start + step);
            copy_recursive(&from_path, &to_path, method, tx, range)?;
        }
        copy_metadata(&metadata, to)?;
    } else {
        copy_file(from, to, &metadata, method, tx, range)?;
    }
    return Ok(());
}

fn copy_file(
    from: &Path,
    to: &Path,
    metadata: &Metadata,
    method: CopyMethod,
    tx: &Sender<ProgressChange>,
    range: Range<f32>,
) -> Rslt<()> {
    match method {
        CopyMethod::CopyFileRange => copy_file_range(from, to, metadata.len(), tx, range),
        CopyMethod::Sendfile => sendfile(from, to, metadata.len(), tx, range),
        CopyMethod::Buffered => buffered(from, to, metadata.len(), tx, range),
    }?;
    copy_metadata(metadata, to)?;
    return Ok(());
}

fn copy_file_range(
    from: &Path,
    to: &Path,
    len: u64,
    tx: &Sender<ProgressChange>,
    range: Range<f32>,
) -> Rslt<()> {
    let src = File::open(from)?;
    let dst = File::create(to)?;

    let src_fd = src.as_raw_fd();
    let dst_fd = dst.as_raw_fd();

    let mut offset_in: off_t = 0;
    let mut offset_out: off_t = 0;
    let mut remaining = len;

    while remaining > 0 {
        let to_copy = remaining.min(MAX_CHUNK as u64) as usize;

        let copied = unsafe {
            libc::syscall(
                libc::SYS_copy_file_range,
                src_fd,
                &mut offset_in as *mut off_t,
                dst_fd,
                &mut offset_out as *mut off_t,
                to_copy,
                0u32,
            )
        };
        if copied < 0 {
            return Err(io::Error::last_os_error().into());
        }
        if copied == 0 {
            return Err(format!("{remaining} {BYTES_LEFT}").into());
        }
        remaining -= copied as u64;
        send(remaining, len, tx, &range)?;
    }
    return Ok(());
}

fn sendfile(
    from: &Path,
    to: &Path,
    len: u64,
    tx: &Sender<ProgressChange>,
    range: Range<f32>,
) -> Rslt<()> {
    let src = File::open(from)?;
    let dst = File::create(to)?;
    let src_fd = src.as_raw_fd();
    let dst_fd = dst.as_raw_fd();
    unsafe {
        libc::posix_fadvise(src_fd, 0, len.try_into()?, libc::POSIX_FADV_SEQUENTIAL);
    }
    let mut offset: off_t = 0;
    let mut remaining = len;
    while remaining > 0 {
        let to_copy = remaining.min(MAX_CHUNK as u64) as usize;
        let copied = unsafe {
            libc::sendfile(dst_fd, src_fd, &mut offset as *mut off_t, to_copy)
        };
        if copied < 0 {
            let errno = io::Error::last_os_error();
            if errno.raw_os_error() == Some(libc::EINVAL) {
                drop(src);
                drop(dst);
                return buffered(from, to, len, tx, range);
            }
            return Err(errno.into());
        }
        if copied == 0 {
            break;
        }
        remaining -= copied as u64;
        send(remaining, len, tx, &range)?;
    }
    return match remaining {
        0 => Ok(()),
        _ => Err(format!("{remaining} {BYTES_LEFT}").into()),
    };
}

fn buffered(
    from: &Path,
    to: &Path,
    len: u64,
    tx: &Sender<ProgressChange>,
    range: Range<f32>,
) -> Rslt<()> {
    let mut src = File::open(from)?;
    let mut dst = File::create(to)?;
    let mut buffer = vec![0u8; BUFFER_SIZE];
    unsafe {
        libc::posix_fadvise(src.as_raw_fd(), 0, 0, libc::POSIX_FADV_SEQUENTIAL);
    }
    let mut remaining = len;
    loop {
        let read = src.read(&mut buffer)?;
        if read == 0 {
            break
        } else {
            dst.write_all(&buffer[..read])?;
            remaining -= read as u64;
            send(remaining, len, tx, &range)?;
        }
    }
    return Ok(());
}

fn copy_metadata(metadata: &Metadata, to: &Path) -> Rslt<()> {
    let permissions = fs::Permissions::from_mode(metadata.mode());
    fs::set_permissions(to, permissions)?;
    unsafe {
        let path_cstr = CString::new(to.as_os_str().as_bytes())
            .map_err(|_| io::Error::new(io::ErrorKind::InvalidInput, INVALID_PATH))?;
        let time = get_times(metadata)?;
        let result = libc::utimensat(libc::AT_FDCWD, path_cstr.as_ptr(), time.as_ptr(), 0);
        if result != OKI {
            return Err(io::Error::last_os_error().into());
        }
    }
    unsafe {
        let path_cstr = CString::new(to.as_os_str().as_bytes()).ok();
        if let Some(path_cstr) = path_cstr {
            libc::chown(path_cstr.as_ptr(), metadata.uid(), metadata.gid());
            // ignore result
        }
    }
    return Ok(());
}

fn get_times(metadata: &Metadata) -> Rslt<[libc::timespec; 2]> {
    let time: [libc::timespec; 2] = [
        libc::timespec {
            tv_sec: metadata.atime().try_into()?,
            tv_nsec: metadata.atime_nsec().try_into()?,
        },
        libc::timespec {
            tv_sec: metadata.mtime().try_into()?,
            tv_nsec: metadata.mtime_nsec().try_into()?,
        },
    ];
    return Ok(time);
}

fn copy_symlink_metadata(metadata: &Metadata, to: &Path) -> Rslt<()> {
    unsafe {
        let path_cstr = CString::new(to.as_os_str().as_bytes())
            .map_err(|_| io::Error::new(io::ErrorKind::InvalidInput, INVALID_PATH))?;
        let time = get_times(metadata)?;
        libc::utimensat(
            libc::AT_FDCWD,
            path_cstr.as_ptr(),
            time.as_ptr(),
            libc::AT_SYMLINK_NOFOLLOW,
        );
        libc::lchown(path_cstr.as_ptr(), metadata.uid(), metadata.gid());
    }
    return Ok(());
}

fn send(
    remaining: u64,
    len: u64,
    tx: &Sender<ProgressChange>,
    range: &Range<f32>,
) -> Rslt<()> {
    let step = range.end - range.start;
    let copied = (len - remaining) as f32;
    let progress = if len == 0 { 1.0 } else { copied / len as f32 };
    let progress = range.start + step * progress;
    let change = match remaining {
        0 => ProgressChange::Increment(progress),
        _ => ProgressChange::Update(progress),
    };
    tx.send(change)?;
    return Ok(());
}
