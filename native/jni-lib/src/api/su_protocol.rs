use bincode::{Decode, Encode};

#[derive(Debug, Encode, Decode, PartialEq)]
pub enum Request {
    GetUsage(String),
    GetMeta(String),
    GetMetas(String),
    GetTypedMeta(String),
    GetTypedMetas(String),
    CreateDir(String),
    CreateFile(String),
    Delete(String),
    TryRun,
}

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
