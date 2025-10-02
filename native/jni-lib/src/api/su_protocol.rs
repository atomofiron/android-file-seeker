use bincode::{Decode, Encode};
use crate::ext::kpath::KPath;

#[derive(Debug, Encode, Decode, PartialEq)]
pub enum Request {
    GetUsage(KPath),
    GetMeta(KPath),
    GetMetas(KPath),
    GetTypedMeta(KPath),
    GetTypedMetas(KPath),
    CreateDir(KPath),
    CreateFile(KPath),
    Delete(KPath),
    TryRun,
}
    //Copy(KPath, KPath, bool),

#[derive(Debug, Encode, Decode, PartialEq)]
pub enum Response {
    Ok(Vec<u8>),
    Err(String),
}

pub type FrameLength = [u8; 4];

pub fn frame_length() -> FrameLength { [0u8; 4] }

pub fn to_len_frame(size: usize) -> FrameLength {
    (size as u32).to_le_bytes()
}

pub fn from_len_frame(buf: FrameLength) -> usize {
    u32::from_le_bytes(buf) as usize
}
