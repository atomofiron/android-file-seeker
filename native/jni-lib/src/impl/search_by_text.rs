use crate::api::protocol::Check;
use crate::api::protocol::{Meta, SearchQuery, SimpleResult, TextSearchCollector, TextSearchProgress};
use crate::common::{Rslt, JOINING_ERROR};
use crate::ext::raw_path::{PathExt, RawPath};
use crate::r#impl::search::progress::proxy_progress;
use crate::r#impl::search::text_matcher::TextMatcher;
use crate::r#impl::search::walker::walk;
use content_inspector::inspect;
use std::fs::File;
use std::io::Read;
use std::path::Path;
use std::sync::mpsc::{channel, Sender};
use std::sync::Arc;
use ignore::WalkState;

pub fn find_text_impl(
    query: SearchQuery,
    targets: Vec<RawPath>,
    max_depth: usize,
    check: Check,
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
    find_text_recursively(matcher, targets, max_depth, check, &tx);
    drop(tx);
    return handle.join()
        .map(|_| SimpleResult::Ok)
        .unwrap_or_else(|_| SimpleResult::Err(JOINING_ERROR.into()));
}

pub fn find_text_recursively(
    matcher: TextMatcher,
    targets: Vec<RawPath>,
    max_depth: usize,
    check: Check,
    sender: &Sender<TextSearchProgress>,
) {
    walk(targets, sender, max_depth, |entry, sender| {
        let path = entry.path();
        let mut error: Option<String> = None;
        if let Check::Yes(max_size) = check {
            match entry.metadata() {
                Err(e) => error = Some(e.to_string()),
                Ok(meta) if !meta.is_file() || meta.len() > max_size || meta.len() == 0 => return WalkState::Continue,
                _ => match is_text_file(path) {
                    Err(e) => error = Some(e.to_string()),
                    Ok(txt) if !txt => return WalkState::Continue,
                    _ => (),
                },
            }
        }
        if let None = error {
            if let Err(e) = matcher.search(path, sender) {
                error = Some(e.to_string());
            }
        }
        let last = match error {
            Some(error) => TextSearchProgress::Err(Meta::with_error(&path.into(), &error)),
            None => TextSearchProgress::End(path.raw()),
        };
        return match sender.send(last) {
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
