use crate::api::protocol::{NameSearchCollector, NameSearchProgress, SearchQuery, SimpleResult};
use crate::common::{Rslt, JOINING_ERROR};
use crate::ext::raw_path::RawPath;
use crate::r#impl::meta::{meta, meta_with_error};
use crate::r#impl::r#type::{file_type, file_type_or_error};
use crate::r#impl::search::progress::proxy_progress;
use crate::r#impl::search::walker::walk;
use std::sync::mpsc::channel;
use std::sync::mpsc::Sender;
use std::sync::Arc;
use ignore::WalkState;
use crate::r#impl::search::matcher::build_matcher;

pub fn find_names_impl(
    query: SearchQuery,
    targets: Vec<RawPath>,
    max_depth: usize,
    exclude_dirs: bool,
    collector: Arc<dyn NameSearchCollector>,
) -> SimpleResult {
    let (tx, rx) = channel::<NameSearchProgress>();
    let handle = match proxy_progress(rx, Box::new(collector)) {
        Ok(handle) => handle,
        Err(e) => return SimpleResult::Err(e.to_string()),
    };
    if let Err(e) = find_names_recursively(query, targets, max_depth, exclude_dirs, &tx) {
        return SimpleResult::Err(e.to_string())
    }
    drop(tx);
    return handle.join()
        .map(|_| SimpleResult::Ok)
        .unwrap_or_else(|_| SimpleResult::Err(JOINING_ERROR.into()));
}

pub fn find_names_recursively(
    query: SearchQuery,
    targets: Vec<RawPath>,
    max_depth: usize,
    exclude_dirs: bool,
    sender: &Sender<NameSearchProgress>,
) -> Rslt<()> {
    let matcher = build_matcher(&query)?;
    walk(targets, sender, max_depth, |entry, sender| {
        let progress = match entry.file_type() {
            None => NameSearchProgress::Err(meta(&entry.path().into())),
            Some(file_type) if exclude_dirs && file_type.is_dir() => NameSearchProgress::Skip,
            _ if matcher.matches(entry.file_name()) => NameSearchProgress::Ok(file_type_or_error(&entry.path().into())),
            _ => NameSearchProgress::Skip,
        };
        return match sender.send(progress) {
            Ok(_) => WalkState::Continue,
            Err(_) => WalkState::Quit,
        };
    });
    return Ok(());
}
