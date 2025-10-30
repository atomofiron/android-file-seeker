use crate::api::protocol::{Meta, SearchQuery, SimpleResult, TextSearchCollector, TextSearchProgress};
use crate::common::{Rslt, JOINING_ERROR};
use crate::ext::raw_path::RawPath;
use crate::r#impl::search::progress::proxy_progress;
use crate::r#impl::search::text_matcher::TextMatcher;
use crate::r#impl::search::text_sink::TextSink;
use crate::r#impl::search::walker::walk;
use content_inspector::inspect;
use grep_searcher::Searcher;
use std::fs::File;
use std::io::Read;
use std::path::Path;
use std::sync::mpsc::{channel, Sender};
use std::sync::Arc;

pub fn find_text_impl(
    query: SearchQuery,
    targets: Vec<RawPath>,
    max_depth: usize,
    max_size: u64,
    collector: Arc<dyn TextSearchCollector>,
) -> SimpleResult {
    let (tx, rx) = channel::<TextSearchProgress>();
    let handle = proxy_progress(rx, Box::new(collector));
    if let Err(e) = find_text_recursively(query, targets, max_depth, max_size, &tx) {
        return SimpleResult::Err(e.to_string())
    }
    drop(tx);
    return handle.join()
        .map(|_| SimpleResult::Ok)
        .unwrap_or_else(|_| SimpleResult::Err(JOINING_ERROR.into()));
}

pub fn find_text_recursively(
    query: SearchQuery,
    targets: Vec<RawPath>,
    max_depth: usize,
    max_size: u64,
    sender: &Sender<TextSearchProgress>,
) -> Rslt<()> {
    let matcher = TextMatcher::new(query)?;
    walk(targets, sender, max_depth, |entry, sender| {
        let path = entry.path();
        let error = match entry.metadata() {
            Ok(meta) if meta.len() > max_size => return,
            Err(e) => e.to_string(),
            _ if !matches!(is_text_file(path), Ok(true)) => return,
            _ => match matcher.search(path, sender) {
                Err(e) => e.to_string(),
                Ok(_) => return,
            }
        };
        let meta = Meta::with_error(&path.into(), &error);
        let err = TextSearchProgress::Err(meta);
        let _ = sender.send(err); // unwrap?
    });
    return Ok(());
}

fn is_text_file(path: &Path) -> Rslt<bool> {
    let mut file = File::open(path)?;
    let mut buf = [0u8; 8192];
    let n = file.read(&mut buf)?;
    let result = inspect(&buf[..n]);
    return Ok(result.is_text());
}
