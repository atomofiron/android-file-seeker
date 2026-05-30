use crate::api::api::{CommonProgress, CommonProgressCollector, NameSearchCollector, NameSearchProgress, SearchQuery, TextSearchCollector, TextSearchProgress};
use crate::ext::raw_path::RawPath;
use bincode::{Decode, Encode};
use std::sync::Arc;

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
    CrcHash(RawPath),
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
        size_limit: Option<u64>,
    },
    ReadFile(RawPath),
}

#[derive(Debug, Encode, Decode, PartialEq)]
pub enum Response {
    Ok(Vec<u8>),
    Err(String),
}

pub const CONTROL_FRAME_LEN: usize = 4;
pub type ControlFrame = [u8; CONTROL_FRAME_LEN];

pub const PID_FRAME: ControlFrame = [0; CONTROL_FRAME_LEN];
pub const FINAL_FRAME: ControlFrame = [0; CONTROL_FRAME_LEN];

pub fn control_frame() -> ControlFrame {
    [0; CONTROL_FRAME_LEN]
}

pub fn len_to_frame(size: usize) -> ControlFrame {
    (size as u32).to_le_bytes()
}

pub fn pid_to_frame(size: u32) -> ControlFrame {
    size.to_le_bytes()
}

pub fn from_control_frame(buf: ControlFrame) -> u32 {
    u32::from_le_bytes(buf)
}

pub trait ProgressProxy<D> : Send + Sync {
    fn emit(&self, progress: D);
}

impl ProgressProxy<CommonProgress> for Arc<dyn CommonProgressCollector> {

    fn emit(&self, progress: CommonProgress) {
        CommonProgressCollector::emit(self.as_ref(), progress)
    }
}

impl ProgressProxy<NameSearchProgress> for Arc<dyn NameSearchCollector> {

    fn emit(&self, progress: NameSearchProgress) {
        NameSearchCollector::emit(self.as_ref(), progress)
    }
}

impl ProgressProxy<TextSearchProgress> for Arc<dyn TextSearchCollector> {

    fn emit(&self, progress: TextSearchProgress) {
        TextSearchCollector::emit(self.as_ref(), progress)
    }
}
