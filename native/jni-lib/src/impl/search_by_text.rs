use crate::api::api::{SearchQuery, SimpleResult, TextSearchCollector, TextSearchProgress};
use crate::api::cancellation::CancellationState;
use crate::common::{Rslt, JOINING_ERROR};
use crate::ext::raw_path::RawPath;
use crate::r#impl::hash::r#impl::crc32;
use crate::r#impl::meta::meta_with_error;
use crate::r#impl::r#type::type_or_meta;
use crate::r#impl::search::progress::proxy_progress;
use crate::r#impl::search::text_matcher::TextMatcher;
use crate::r#impl::search::walker::walk;
use content_inspector::inspect;
use ignore::WalkState;
use std::fs::File;
use std::io::Read;
use std::path::Path;
use std::sync::mpsc::{channel, Sender};
use std::sync::Arc;

pub fn find_text_impl(
    query: SearchQuery,
    targets: Vec<RawPath>,
    max_depth: usize,
    size_limit: Option<u64>,
    cancellation: Arc<dyn CancellationState>,
    collector: Arc<dyn TextSearchCollector>,
) -> SimpleResult {
    let (tx, rx) = channel::<TextSearchProgress>();
    let handle = match proxy_progress(rx, Box::new(collector)) {
        Ok(handle) => handle,
        Err(e) => return SimpleResult::Err(e.to_string()),
    };
    let matcher = match TextMatcher::new(query) {
        Ok(m) => m,
        Err(e) => return SimpleResult::Err(e.to_string())
    };
    find_text_recursively(matcher, targets, max_depth, size_limit, cancellation, &tx);
    drop(tx);
    return handle.join()
        .map(|_| SimpleResult::Ok)
        .unwrap_or_else(|_| SimpleResult::Err(JOINING_ERROR.into()));
}

pub fn find_text_recursively(
    matcher: TextMatcher,
    targets: Vec<RawPath>,
    max_depth: usize,
    size_limit: Option<u64>,
    cancellation: Arc<dyn CancellationState>,
    sender: &Sender<TextSearchProgress>,
) {
    walk(targets, sender, max_depth, cancellation, |entry, sender| {
        let path = entry.path();
        let progress = match size_limit {
            Some(size_limit) => {
                match entry.metadata() {
                    Err(e) => Err(e.into()),
                    Ok(meta) if !meta.is_file() || meta.len() > size_limit || meta.len() == 0 => return WalkState::Continue,
                    _ => match is_text_file(path) {
                        Err(e) => Err(e.into()),
                        Ok(txt) if !txt => return WalkState::Continue,
                        _ => Ok(()),
                    },
                }
            }
            None => Ok(())
        }.and_then(|_| matcher.search(path))
            .map(|matches| match matches.is_empty() {
                true => TextSearchProgress::Skip,
                false => TextSearchProgress::Match(type_or_meta(&path.into()), crc32(path), matches),
            }).unwrap_or_else(|e| TextSearchProgress::Err(meta_with_error(&path.into(), &e)));
        return match sender.send(progress) {
            Ok(_) => WalkState::Continue,
            Err(_) => WalkState::Quit,
        };
    });
}

fn is_text_file(path: &Path) -> Rslt<bool> {
    let mut file = File::open(path)?;
    let mut buf = [0u8; 8192];
    let n = file.read(&mut buf)?;
    let result = inspect(&buf[..n]);
    return Ok(result.is_text());
}
