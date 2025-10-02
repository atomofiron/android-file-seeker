use std::sync::Arc;
use crate::api::protocol::{CopyCollector, DeleteResult, MetaResult, MetasResult, SimpleResult, TypedMetaResult, TypedMetasResult, UsageResult};
use crate::api::su_bridge::as_su;
use crate::api::su_protocol::Request;
use crate::ext::kpath::{KPath, KPathExt};
use crate::r#impl::copy::copy_impl;
use crate::r#impl::delete::delete;
use crate::r#impl::meta::{meta, meta_with_error, metas};
use crate::r#impl::other::{new_dir, new_file, usage};
use crate::r#impl::r#type::{file_type, file_types};

#[uniffi::export]
pub fn create_file(path: KPath, run_as_su: Option<String>) -> MetaResult {
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
pub fn create_dir(path: KPath, run_as_su: Option<String>) -> MetaResult {
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
pub fn delete_by(path: KPath, run_as_su: Option<String>) -> DeleteResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<DeleteResult>(Request::Delete(path), bin_path)
            .unwrap_or_else(|e| DeleteResult::Err(e.to_string(), None))
    }
    let path = path.buf();
    match delete(&path) {
        Ok(0) => DeleteResult::Ok(meta(&path).ok()),
        Ok(err_count) => DeleteResult::ErrCount(err_count),
        Err(e) => DeleteResult::Err(e.to_string(), Some(meta_with_error(&path, e.to_string()))),
    }
}

#[uniffi::export]
pub fn get_usage(path: KPath, run_as_su: Option<String>) -> UsageResult {
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
pub fn get_meta(path: KPath, run_as_su: Option<String>) -> MetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<MetaResult>(Request::GetMeta(path), bin_path)
            .unwrap_or_else(|e| MetaResult::Err(e.to_string()))
    }
    match meta(&path.buf()) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_metas(path: KPath, run_as_su: Option<String>) -> MetasResult {
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
pub fn get_file_type(path: KPath, run_as_su: Option<String>) -> TypedMetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<TypedMetaResult>(Request::GetTypedMeta(path), bin_path)
            .unwrap_or_else(|e| TypedMetaResult::Err(e.to_string()))
    }
    match file_type(&path.buf()) {
        Ok(data) => TypedMetaResult::Ok(data),
        Err(e) => TypedMetaResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_file_types(path: KPath, run_as_su: Option<String>) -> TypedMetasResult {
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
    from: KPath,
    to: KPath,
    moving: bool,
    run_as_su: Option<String>,
    collector: Arc<dyn CopyCollector>,
) -> SimpleResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<SimpleResult>(Request::Copy(from, to, moving), bin_path)
            .unwrap_or_else(|e| SimpleResult::Err(e.to_string()))
    }
    match copy_impl(from.buf(), to.buf(), moving, collector) {
        Ok(_data) => SimpleResult::Ok,
        Err(e) => SimpleResult::Err(e.to_string()),
    }
}
