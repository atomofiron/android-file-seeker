use crate::api::protocol::{DeleteResult, MetaResult, MetasResult, TypedMetaResult, TypedMetasResult, UsageResult};
use crate::api::su_bridge::as_su;
use crate::api::su_protocol::Request;
use crate::r#impl::delete::delete;
use crate::r#impl::meta::{meta, meta_with_error, metas};
use crate::r#impl::other::{new_dir, new_file, usage};
use crate::r#impl::r#type::{file_type, file_types};

#[uniffi::export]
pub fn create_file(path: String, run_as_su: Option<String>) -> MetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<MetaResult>(Request::GetMeta(path), bin_path)
            .unwrap_or_else(|e| MetaResult::Err(e.to_string()))
    }
    match new_file(&path) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn create_dir(path: String, run_as_su: Option<String>) -> MetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<MetaResult>(Request::GetMeta(path), bin_path)
            .unwrap_or_else(|e| MetaResult::Err(e.to_string()))
    }
    match new_dir(&path) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn delete_by(path: String, run_as_su: Option<String>) -> DeleteResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<DeleteResult>(Request::Delete(path), bin_path)
            .unwrap_or_else(|e| DeleteResult::Err(e.to_string(), None))
    }
    match delete(&path) {
        Ok(0) => DeleteResult::Ok(meta(&path).ok()),
        Ok(err_count) => DeleteResult::ErrCount(err_count),
        Err(e) => DeleteResult::Err(e.to_string(), Some(meta_with_error(&path, e.to_string()))),
    }
}

#[uniffi::export]
pub fn get_usage(path: String, run_as_su: Option<String>) -> UsageResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<UsageResult>(Request::GetUsage(path), bin_path)
            .unwrap_or_else(|e| UsageResult::Err(e.to_string()))
    }
    match usage(&path) {
        Ok(data) => UsageResult::Ok(data),
        Err(e) => UsageResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_meta(path: String, run_as_su: Option<String>) -> MetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<MetaResult>(Request::GetMeta(path), bin_path)
            .unwrap_or_else(|e| MetaResult::Err(e.to_string()))
    }
    match meta(&path) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_metas(path: String, run_as_su: Option<String>) -> MetasResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<MetasResult>(Request::GetMetas(path), bin_path)
            .unwrap_or_else(|e| MetasResult::Err(e.to_string()))
    }
    match metas(&path) {
        Ok(data) => MetasResult::Ok(data),
        Err(e) => MetasResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_file_type(path: String, run_as_su: Option<String>) -> TypedMetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<TypedMetaResult>(Request::GetTypedMeta(path), bin_path)
            .unwrap_or_else(|e| TypedMetaResult::Err(e.to_string()))
    }
    match file_type(&path) {
        Ok(data) => TypedMetaResult::Ok(data),
        Err(e) => TypedMetaResult::Err(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_file_types(path: String, run_as_su: Option<String>) -> TypedMetasResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<TypedMetasResult>(Request::GetTypedMetas(path), bin_path)
            .unwrap_or_else(|e| TypedMetasResult::Err(e.to_string()))
    }
    match file_types(&path) {
        Ok(data) => TypedMetasResult::Ok(data),
        Err(e) => TypedMetasResult::Err(e.to_string()),
    }
}
