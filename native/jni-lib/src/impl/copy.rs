use std::path::PathBuf;
use std::sync::Arc;
use crate::api::protocol::{CopyCollector};
use crate::common::Rslt;

pub fn copy_impl(
    _from: PathBuf,
    _to: PathBuf,
    _moving: bool,
    _collector: Arc<dyn CopyCollector>,
) -> Rslt<()> {
    Err("Not implemented".into())
}
