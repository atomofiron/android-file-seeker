use crate::api::protocol::{CommonProgressCollector, ComplexResult, MetaResult, MetasResult, NameSearchCollector, SearchQuery, SimpleResult, TextSearchCollector, TypedMetaResult, TypedMetasResult, UsageResult};
use crate::api::su_bridge::{as_su, as_su_with_progress};
use crate::api::su_protocol::Request;
use crate::ext::raw_path::{RawPath, RawPathExt};
use crate::r#impl::copy::copy_impl;
use crate::r#impl::delete::delete_impl;
use crate::r#impl::meta::{try_meta, meta_with_error, metas};
use crate::r#impl::other::{new_dir, new_file, usage};
use crate::r#impl::r#type::{file_type_impl, file_types};
use crate::r#impl::search_by_name::find_names_impl;
use crate::r#impl::search_by_text::find_text_impl;
use std::sync::Arc;

#[uniffi::export]
pub fn create_file(path: RawPath, run_as_su: Option<String>) -> MetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<MetaResult>(Request::GetMeta(path), bin_path)
            .unwrap_or_else(|e| MetaResult::Err(e.to_string()))
    }
    match new_file(&path.buf()) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn create_dir(path: RawPath, run_as_su: Option<String>) -> MetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<MetaResult>(Request::GetMeta(path), bin_path)
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
    run_as_su: Option<String>,
    collector: Arc<dyn CommonProgressCollector>,
) -> ComplexResult {
    if let Some(bin_path) = run_as_su {
        let from_buf = path.clone().buf();
        return as_su_with_progress(Request::Delete(path), bin_path, Box::new(collector))
            .unwrap_or_else(|e| ComplexResult::Err(meta_with_error(&from_buf, &e)));
    }
    let path = path.buf();
    match delete_impl(&path, collector) {
        Ok(result) => result,
        Err(e) => ComplexResult::Err(meta_with_error(&path, &e)),
    }
}

#[uniffi::export]
pub fn get_usage(path: RawPath, run_as_su: Option<String>) -> UsageResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<UsageResult>(Request::GetUsage(path), bin_path)
            .unwrap_or_else(|e| UsageResult::Err(e.to_string()))
    }
    match usage(&path.buf()) {
        Ok(data) => UsageResult::Ok(data),
        Err(e) => UsageResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_meta(path: RawPath, run_as_su: Option<String>) -> MetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<MetaResult>(Request::GetMeta(path), bin_path)
            .unwrap_or_else(|e| MetaResult::Err(e.to_string()))
    }
    match try_meta(&path.buf()) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_metas(path: RawPath, run_as_su: Option<String>) -> MetasResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<MetasResult>(Request::GetMetas(path), bin_path)
            .unwrap_or_else(|e| MetasResult::Err(e.to_string()))
    }
    match metas(&path.buf()) {
        Ok(data) => MetasResult::Ok(data),
        Err(e) => MetasResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_file_type(path: RawPath, run_as_su: Option<String>) -> TypedMetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<TypedMetaResult>(Request::GetTypedMeta(path), bin_path)
            .unwrap_or_else(|e| TypedMetaResult::Err(e.to_string()))
    }
    match file_type_impl(&path.buf()) {
        Ok(data) => TypedMetaResult::Ok(data),
        Err(e) => TypedMetaResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_file_types(path: RawPath, run_as_su: Option<String>) -> TypedMetasResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<TypedMetasResult>(Request::GetTypedMetas(path), bin_path)
            .unwrap_or_else(|e| TypedMetasResult::Err(e.to_string()))
    }
    match file_types(&path.buf()) {
        Ok(data) => TypedMetasResult::Ok(data),
        Err(e) => TypedMetasResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn copy(
    from: RawPath,
    to: RawPath,
    moving: bool,
    run_as_su: Option<String>,
    collector: Arc<dyn CommonProgressCollector>,
) -> ComplexResult {
    if let Some(bin_path) = run_as_su {
        let from_buf = from.clone().buf();
        return as_su_with_progress(Request::Copy(from, to, moving), bin_path, Box::new(collector))
            .unwrap_or_else(|e| ComplexResult::Err(meta_with_error(&from_buf, &e)))
    }
    let from_buf = from.buf();
    match copy_impl(&from_buf, &to.buf(), moving, collector) {
        Ok(result) => result,
        Err(e) => ComplexResult::Err(meta_with_error(&from_buf, &e)),
    }
}

#[uniffi::export]
pub fn find_names(
    query: SearchQuery,
    targets: Vec<RawPath>,
    max_depth: u32,
    exclude_dirs: bool,
    run_as_su: Option<String>,
    collector: Arc<dyn NameSearchCollector>,
) -> SimpleResult {
    if let Some(bin_path) = run_as_su {
        return as_su_with_progress(Request::FindNames { query, targets, max_depth, exclude_dirs }, bin_path, Box::new(collector))
            .unwrap_or_else(|e| SimpleResult::Err(e.to_string()))
    }
    return find_names_impl(query, targets, max_depth as usize, exclude_dirs, collector);
}

#[uniffi::export]
pub fn find_text(
    query: SearchQuery,
    targets: Vec<RawPath>,
    max_depth: u32,
    max_size: u64,
    run_as_su: Option<String>,
    collector: Arc<dyn TextSearchCollector>,
) -> SimpleResult {
    if let Some(bin_path) = run_as_su {
        return as_su_with_progress(Request::FindText { query, targets, max_depth, max_size }, bin_path, Box::new(collector))
            .unwrap_or_else(|e| SimpleResult::Err(e.to_string()))
    }
    return find_text_impl(query, targets, max_depth as usize, max_size, collector);
}
