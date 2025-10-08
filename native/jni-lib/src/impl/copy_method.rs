use std::io;
use crate::common::{OKL, OKS};

const INVALID_DF: i32 = -1;

#[derive(Debug, Clone, Copy)]
pub enum CopyMethod {
    CopyFileRange,
    Sendfile,
    Buffered,
}

impl CopyMethod {

    pub fn detect() -> Self {
        match () {
            _ if Self::is_copy_file_range_available() => CopyMethod::CopyFileRange,
            _ if Self::is_sendfile_available() => CopyMethod::Sendfile,
            _ => CopyMethod::Buffered,
        }
    }

    pub fn is_copy_file_range_available() -> bool {
        unsafe {
            let result = libc::syscall(
                libc::SYS_copy_file_range,
                INVALID_DF,
                std::ptr::null_mut::<i64>(),
                INVALID_DF,
                std::ptr::null_mut::<i64>(),
                0usize,
                0u32,
            );
            return result == OKL || io::Error::last_os_error().raw_os_error() != Some(libc::ENOSYS);
        }
    }

    pub fn is_sendfile_available() -> bool {
        unsafe {
            let result = libc::sendfile(INVALID_DF, INVALID_DF, std::ptr::null_mut(), 0);
            return result == OKS || io::Error::last_os_error().raw_os_error() != Some(libc::ENOSYS);
        }
    }
}
