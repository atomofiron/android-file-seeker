use std::ffi::{OsStr, OsString};
use std::os::unix::ffi::{OsStrExt, OsStringExt};
use std::path::{Path, PathBuf};

pub type RawPath = Vec<u8>;

pub trait RawPathExt {
    fn buf(self) -> PathBuf;
    fn hidden(&self) -> bool;
}

impl RawPathExt for RawPath {

    fn buf(self) -> PathBuf {
        PathBuf::from(OsString::from_vec(self))
    }

    fn hidden(&self) -> bool {
        matches!(self.first(), Some(b'.'))
    }
}

pub trait PathExt {
    fn raw(&self) -> RawPath;
}

impl PathExt for Path {

    fn raw(&self) -> RawPath {
        self.as_os_str().as_bytes().to_vec()
    }
}

impl PathExt for PathBuf {

    fn raw(&self) -> RawPath {
        self.as_os_str().as_bytes().to_vec()
    }
}

impl PathExt for OsStr {

    fn raw(&self) -> RawPath {
        self.as_bytes().into()
    }
}
