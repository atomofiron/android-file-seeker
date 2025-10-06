use std::ffi::OsString;
use std::os::unix::ffi::OsStringExt;
use std::path::PathBuf;

pub type RawPath = Vec<u8>;

pub trait KPathExt {
    fn buf(self) -> PathBuf;
}

impl KPathExt for RawPath {
    fn buf(self) -> PathBuf {
        PathBuf::from(OsString::from_vec(self))
    }
}
