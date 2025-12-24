use crate::common::Rslt;
use crc32fast::Hasher;
use std::fs::File;
use std::io::Read;
use std::path::Path;
use crate::api::api::CrcResult;

pub fn crc32(path: &Path) -> CrcResult {
    match try_crc32(path) {
        Ok(v) => CrcResult::Ok(v),
        Err(e) => CrcResult::Err(e.to_string()),
    }
}

pub fn try_crc32(path: &Path) -> Rslt<u32> {
    let mut file = File::open(path)?;
    let mut hasher = Hasher::new();
    let mut buf = [0u8; 64 * 1024];
    loop {
        let n = file.read(&mut buf)?;
        if n == 0 {
            break;
        }
        hasher.update(&buf[..n]);
    }
    Ok(hasher.finalize())
}
