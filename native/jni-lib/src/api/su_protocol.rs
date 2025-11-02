use std::sync::Arc;
use bincode::{Decode, Encode};
use crate::api::protocol::{NameSearchCollector, NameSearchProgress, CommonProgress, CommonProgressCollector, SearchQuery, TextSearchCollector, TextSearchProgress, Check};
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
    FindNames {
        query: SearchQuery,
        targets: Vec<RawPath>,
        max_depth: u32,
        exclude_dirs: bool,
    },
    FindText {
        query: SearchQuery,
        targets: Vec<RawPath>,
        max_depth: u32,
        check: Check,
    },
}

#[derive(Debug, Encode, Decode, PartialEq)]
pub enum Response {
    Ok(Vec<u8>),
    Err(String),
}

const CONTROL_FRAME_LEN: usize = 4;
pub type FrameLength = [u8; CONTROL_FRAME_LEN];

pub const FINAL_FRAME: FrameLength = [0; CONTROL_FRAME_LEN];

pub fn frame_length() -> FrameLength { [0; CONTROL_FRAME_LEN] }

pub fn to_len_frame(size: usize) -> FrameLength {
    (size as u32).to_le_bytes()
}

pub fn from_len_frame(buf: FrameLength) -> usize {
    u32::from_le_bytes(buf) as usize
}

pub trait ProgressProxy<D> : Send + Sync {
    fn emit(&self, progress: D) -> bool;
}

impl ProgressProxy<CommonProgress> for Arc<dyn CommonProgressCollector> {

    fn emit(&self, progress: CommonProgress) -> bool {
        CommonProgressCollector::emit(self.as_ref(), progress)
    }
}

impl ProgressProxy<NameSearchProgress> for Arc<dyn NameSearchCollector> {

    fn emit(&self, progress: NameSearchProgress) -> bool {
        NameSearchCollector::emit(self.as_ref(), progress)
    }
}

impl ProgressProxy<TextSearchProgress> for Arc<dyn TextSearchCollector> {

    fn emit(&self, progress: TextSearchProgress) -> bool {
        TextSearchCollector::emit(self.as_ref(), progress)
    }
}
