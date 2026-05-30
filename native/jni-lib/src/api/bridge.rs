use crate::api::api::{CrcResult, FileEventCollector, HandleResult, SuCmd};
use crate::api::api::{CommonProgressCollector, CountingResult, MetaResult, MetasResult, NameSearchCollector, SearchQuery, SimpleResult, TextSearchCollector, TypedMetaResult, TypedMetasResult, UsageResult};
use crate::api::cancellation::CancellationState;
use crate::api::su_api::Request;
use crate::api::su_bridge::{as_su, as_su_with_progress, get_child, write_request};
use crate::ext::raw_path::{RawPath, RawPathExt};
use crate::ext::result::ResultExt;
use crate::r#impl::copy::copy_impl;
use crate::r#impl::delete::delete_impl;
use crate::r#impl::hash::r#impl::crc32;
use crate::r#impl::inotify::r#impl::try_observe_dir;
use crate::r#impl::meta::{meta_with_error, metas, try_meta};
use crate::r#impl::other::{new_dir, new_file, usage};
use crate::r#impl::r#type::{file_type, file_types};
use crate::r#impl::reader::api::{FileReader, ReaderResult};
use crate::r#impl::search_by_name::find_names_impl;
use crate::r#impl::search_by_text::find_text_impl;
use std::fs::File;
use std::sync::Arc;

#[uniffi::export]
pub fn create_file(path: RawPath, su_cmd: Option<SuCmd>) -> MetaResult {
    if let Some(su_cmd) = su_cmd {
        return as_su::<MetaResult>(Request::CreateFile(path), su_cmd)
            .unwrap_or_else(|e| MetaResult::Err(e.to_string()))
    }
    match new_file(&path.buf()) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn create_dir(path: RawPath, su_cmd: Option<SuCmd>) -> MetaResult {
    if let Some(su_cmd) = su_cmd {
        return as_su::<MetaResult>(Request::CreateDir(path), su_cmd)
            .unwrap_or_else(|e| MetaResult::Err(e.to_string()))
    }
    match new_dir(&path.buf()) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn delete_by(
    path: RawPath,
    su_cmd: Option<SuCmd>,
    collector: Arc<dyn CommonProgressCollector>,
) -> CountingResult {
    if let Some(su_cmd) = su_cmd {
        let from_buf = path.clone().buf();
        return as_su_with_progress(
            Request::Delete(path),
            su_cmd,
            Arc::new(()),
            Box::new(collector),
        ).unwrap_or_else(|e| CountingResult::Err(meta_with_error(&from_buf, &e)));
    }
    let path = path.buf();
    match delete_impl(&path, collector) {
        Ok(result) => result,
        Err(e) => CountingResult::Err(meta_with_error(&path, &e)),
    }
}

#[uniffi::export]
pub fn get_usage(path: RawPath, su_cmd: Option<SuCmd>) -> UsageResult {
    if let Some(su_cmd) = su_cmd {
        return as_su::<UsageResult>(Request::GetUsage(path), su_cmd)
            .unwrap_or_else(|e| UsageResult::Err(e.to_string()))
    }
    match usage(&path.buf()) {
        Ok((length, size)) => UsageResult::Ok(length, size),
        Err(e) => UsageResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_meta(path: RawPath, su_cmd: Option<SuCmd>) -> MetaResult {
    if let Some(su_cmd) = su_cmd {
        return as_su::<MetaResult>(Request::GetMeta(path), su_cmd)
            .unwrap_or_else(|e| MetaResult::Err(e.to_string()))
    }
    match try_meta(&path.buf()) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_metas(path: RawPath, su_cmd: Option<SuCmd>) -> MetasResult {
    if let Some(su_cmd) = su_cmd {
        return as_su::<MetasResult>(Request::GetMetas(path), su_cmd)
            .unwrap_or_else(|e| MetasResult::Err(e.to_string()))
    }
    match metas(&path.buf()) {
        Ok(data) => MetasResult::Ok(data),
        Err(e) => MetasResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_file_type(path: RawPath, su_cmd: Option<SuCmd>) -> TypedMetaResult {
    if let Some(su_cmd) = su_cmd {
        return as_su::<TypedMetaResult>(Request::GetTypedMeta(path), su_cmd)
            .unwrap_or_else(|e| TypedMetaResult::Err(e.to_string()))
    }
    match file_type(&path.buf()) {
        Ok(data) => TypedMetaResult::Ok(data),
        Err(e) => TypedMetaResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_file_types(path: RawPath, su_cmd: Option<SuCmd>) -> TypedMetasResult {
    if let Some(su_cmd) = su_cmd {
        return as_su::<TypedMetasResult>(Request::GetTypedMetas(path), su_cmd)
            .unwrap_or_else(|e| TypedMetasResult::Err(e.to_string()))
    }
    match file_types(&path.buf()) {
        Ok(data) => TypedMetasResult::Ok(data),
        Err(e) => TypedMetasResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn crc_hash(path: RawPath, su_cmd: Option<SuCmd>) -> CrcResult {
    if let Some(su_cmd) = su_cmd {
        return as_su::<CrcResult>(Request::CrcHash(path), su_cmd)
            .unwrap_or_else(|e| CrcResult::Err(e.to_string()))
    }
    return crc32(&path.buf())
}

#[uniffi::export]
pub fn copy(
    from: RawPath,
    to: RawPath,
    moving: bool,
    su_cmd: Option<SuCmd>,
    collector: Arc<dyn CommonProgressCollector>,
) -> CountingResult {
    if let Some(su_cmd) = su_cmd {
        let from_buf = from.clone().buf();
        return as_su_with_progress(
            Request::Copy(from, to, moving),
            su_cmd,
            Arc::new(()),
            Box::new(collector),
        ).unwrap_or_else(|e| CountingResult::Err(meta_with_error(&from_buf, &e)))
    }
    let from_buf = from.buf();
    return match copy_impl(&from_buf, &to.buf(), moving, collector) {
        Ok(result) => result,
        Err(e) => CountingResult::Err(meta_with_error(&from_buf, &e)),
    }
}

#[uniffi::export]
pub fn find_names(
    query: SearchQuery,
    targets: Vec<RawPath>,
    max_depth: u32,
    exclude_dirs: bool,
    su_cmd: Option<SuCmd>,
    cancellation: Arc<dyn CancellationState>,
    collector: Arc<dyn NameSearchCollector>,
) -> SimpleResult {
    if let Some(su_cmd) = su_cmd {
        return as_su_with_progress(
            Request::FindNames { query, targets, max_depth, exclude_dirs },
            su_cmd,
            cancellation,
            Box::new(collector),
        ).unwrap_or_else(|e| SimpleResult::Err(e.to_string()))
    }
    return find_names_impl(query, targets, max_depth as usize, exclude_dirs, cancellation, collector);
}

#[uniffi::export]
pub fn find_text(
    query: SearchQuery,
    targets: Vec<RawPath>,
    max_depth: u32,
    size_limit: Option<u64>,
    su_cmd: Option<SuCmd>,
    cancellation: Arc<dyn CancellationState>,
    collector: Arc<dyn TextSearchCollector>,
) -> SimpleResult {
    if let Some(su_cmd) = su_cmd {
        return as_su_with_progress(
            Request::FindText { query, targets, max_depth, size_limit },
            su_cmd,
            cancellation,
            Box::new(collector),
        ).unwrap_or_else(|e| SimpleResult::Err(e.to_string()))
    }
    return find_text_impl(query, targets, max_depth as usize, size_limit, cancellation, collector);
}

#[uniffi::export]
pub fn observe_dir(target: RawPath, collector: Arc<dyn FileEventCollector>) -> HandleResult {
    match try_observe_dir(target, collector) {
        Ok(handle) => HandleResult::Ok(handle),
        Err(e) => HandleResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn read_file(path: RawPath, su_cmd: Option<SuCmd>) -> ReaderResult {
    let reader = match su_cmd {
        Some(su_cmd) => get_child(&su_cmd).and_then(|(mut child, pid)| {
            let request = Request::ReadFile(path);
            write_request(&mut child, request)?;
            Ok(FileReader::with(child, pid))
        }),
        None => File::open(path.buf())
            .map(|p| FileReader::new(p))
            .boxed(),
    };
    return match reader {
        Ok(r) => ReaderResult::Ok(Arc::new(r)),
        Err(e) => ReaderResult::Err(e.to_string()),
    }
}
