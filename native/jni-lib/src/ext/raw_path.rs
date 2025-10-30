use std::ffi::OsString;
use std::os::unix::ffi::{OsStrExt, OsStringExt};
use std::path::PathBuf;

pub type RawPath = Vec<u8>;

pub trait RawPathExt {
    fn buf(self) -> PathBuf;
}

impl RawPathExt for RawPath {

    fn buf(self) -> PathBuf {
        PathBuf::from(OsString::from_vec(self))
    }
}

pub trait PathBufExt {
    fn raw(self) -> RawPath;
}

impl PathBufExt for PathBuf {

    fn raw(self) -> RawPath {
        self.as_os_str().as_bytes().to_vec()
    }
}
