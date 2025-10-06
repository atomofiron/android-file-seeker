use bincode::{Decode, Encode};
use crate::ext::raw_path::RawPath;

#[derive(Debug, Encode, Decode, PartialEq)]
pub enum Request {
    GetUsage(RawPath),
    GetMeta(RawPath),
    GetMetas(RawPath),
    GetTypedMeta(RawPath),
    GetTypedMetas(RawPath),
    CreateDir(RawPath),
    CreateFile(RawPath),
    Delete(RawPath),
    TryRun,
    Copy(RawPath, RawPath, bool),
}

#[derive(Debug, Encode, Decode, PartialEq)]
pub enum Response {
    Ok(Vec<u8>),
    Err(String),
}

pub type FrameLength = [u8; 4];

#[allow(dead_code)]
const FINAL_FRAME: FrameLength = [0; 4];

pub fn frame_length() -> FrameLength { [0; 4] }

pub fn to_len_frame(size: usize) -> FrameLength {
    (size as u32).to_le_bytes()
}

pub fn from_len_frame(buf: FrameLength) -> usize {
    u32::from_le_bytes(buf) as usize
}
