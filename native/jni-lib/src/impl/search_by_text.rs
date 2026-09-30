use crate::api::api::{SearchQuery, SimpleResult, TextSearchCollector, TextSearchProgress};
use crate::api::cancellation::CancellationState;
use crate::common::{string, Rslt, CHARSET_MISMATCH, JOINING_ERROR, WRONG_CHARSET};
use crate::ext::option::OptionExt;
use crate::ext::raw_path::RawPath;
use crate::r#impl::hash::r#impl::crc32;
use crate::r#impl::meta::meta_with_error;
use crate::r#impl::r#type::type_or_meta;
use crate::r#impl::search::progress::proxy_progress;
use crate::r#impl::search::text_matcher::TextMatcher;
use crate::r#impl::search::walker::walk;
use content_inspector::inspect;
use encoding_rs::Encoding as EncodingRs;
use encoding_rs::{UTF_16BE, UTF_16LE};
use grep_regex::RegexMatcher;
use grep_searcher::Encoding;
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
    charset: String,
    cancellation: Arc<dyn CancellationState>,
    collector: Arc<dyn TextSearchCollector>,
) -> SimpleResult {
    let (tx, rx) = channel::<TextSearchProgress>();
    let handle = match proxy_progress(rx, Box::new(collector)) {
        Ok(handle) => handle,
        Err(e) => return SimpleResult::Err(e.to_string()),
    };
    let encoding = match Encoding::new(&charset) {
        Ok(en) => en,
        Err(e) => return SimpleResult::Err(e.to_string()),
    };
    let matcher = match RegexMatcher::from_query(query) {
        Ok(m) => m,
        Err(e) => return SimpleResult::Err(e.to_string())
    };
    find_text_recursively(matcher, targets, max_depth, charset, encoding, size_limit, cancellation, &tx);
    drop(tx);
    return handle.join()
        .map(|_| SimpleResult::Ok)
        .unwrap_or_else(|_| SimpleResult::Err(JOINING_ERROR.into()));
}

pub fn find_text_recursively(
    matcher: RegexMatcher,
    targets: Vec<RawPath>,
    max_depth: usize,
    charset: String,
    encoding: Encoding,
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
        }.and_then(|_| {
            let (encoding, bom_offset) = resolve(encoding.clone(), &charset, path)?;
            matcher.search(path, encoding, bom_offset)
        }).map(|matches| match matches.is_empty() {
            true => TextSearchProgress::Skip,
            false => TextSearchProgress::Match(type_or_meta(&path.into()), crc32(path), matches),
        }).unwrap_or_else(|e| TextSearchProgress::Err(meta_with_error(&path.into(), &e)));
        return match sender.send(progress) {
            Ok(_) => WalkState::Continue,
            Err(_) => WalkState::Quit,
        };
    });
}

// tries to resolve UTF_16 to UTF_16LE or UTF_16BE
fn resolve(encoding: Encoding, charset: &str, path: &Path) -> Rslt<(Encoding, u64)> {
    let len = charset.len();
    if len != 6 && len != 8 {
        return Ok((encoding, 0))
    }
    let en_rs = EncodingRs::for_label(charset.as_bytes())
        .or_err(|| string(WRONG_CHARSET))?;
    if en_rs != UTF_16LE && en_rs != UTF_16BE {
        return Ok((encoding, 0))
    }
    let mut file = File::open(path)?;
    let mut bytes = [0u8; 2];
    file.read_exact(&mut bytes)?;
    let encoding = match bytes {
        [0xFF, 0xFE] => match len {
            6 => Encoding::new(UTF_16LE.name())?,
            _ if en_rs == UTF_16BE => return Err(string(CHARSET_MISMATCH).into()),
            _ => encoding, // UTF_16LE
        }
        [0xFE, 0xFF] => match len {
            6 => Encoding::new(UTF_16BE.name())?,
            _ if en_rs == UTF_16LE => return Err(string(CHARSET_MISMATCH).into()),
            _ => encoding, // UTF_16BE
        }
        _ if len == 6 => return Err(string(CHARSET_MISMATCH).into()),
        _ => encoding // wdc
    };
    let bom_offset = match bytes {
        [0xFF, 0xFE] | [0xFE, 0xFF] => 3, // UTF-8 bytes
        _ => 0,
    };
    return Ok((encoding, bom_offset))
}

fn is_text_file(path: &Path) -> Rslt<bool> {
    let mut file = File::open(path)?;
    let mut buf = [0u8; 8192];
    let n = file.read(&mut buf)?;
    let result = inspect(&buf[..n]);
    return Ok(result.is_text());
}
