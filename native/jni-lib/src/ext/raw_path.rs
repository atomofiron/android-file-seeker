use std::ffi::OsString;
use std::os::unix::ffi::{OsStrExt, OsStringExt};
use std::path::{Path, PathBuf};

pub type RawPath = Vec<u8>;

pub trait RawPathExt {
    fn buf(self) -> PathBuf;
    fn raw(self) -> RawPath;
}

impl RawPathExt for RawPath {

    fn buf(self) -> PathBuf {
        PathBuf::from(OsString::from_vec(self))
    }

    fn raw(self) -> RawPath {
        self
    }
}

impl RawPathExt for PathBuf {

    fn buf(self) -> PathBuf {
        self
    }

    fn raw(self) -> RawPath {
        self.as_os_str().as_bytes().to_vec()
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
