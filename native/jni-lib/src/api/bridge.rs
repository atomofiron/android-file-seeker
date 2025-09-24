use crate::r#impl::meta::{meta, metas};
use crate::r#impl::other::{delete, new_dir, new_file, usage};
use crate::r#impl::r#type::{file_type, file_types};
use crate::api::protocol::{DeleteResult, MetaResult, MetasResult, TypedMetaResult, TypedMetasResult, UsageResult};

#[uniffi::export]
fn create_file(path: String) -> MetaResult {
    match new_file(path) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Error(e.to_string()),
    }
}

#[uniffi::export]
fn create_dir(path: String) -> MetaResult {
    match new_dir(path) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Error(e.to_string()),
    }
}

#[uniffi::export]
fn delete_by(path: String) -> DeleteResult {
    match delete(path) {
        Ok(_) => DeleteResult::Ok,
        Err(e) => DeleteResult::Error(e.to_string()),
    }
}

#[uniffi::export]
fn get_usage(path: String) -> UsageResult {
    match usage(path) {
        Ok(data) => UsageResult::Ok(data),
        Err(e) => UsageResult::Error(e.to_string()),
    }
}

#[uniffi::export]
fn get_meta(path: String) -> MetaResult {
    match meta(path) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Error(e.to_string()),
    }
}

#[uniffi::export]
fn get_metas(path: String) -> MetasResult {
    match metas(path) {
        Ok(data) => MetasResult::Ok(data),
        Err(e) => MetasResult::Error(e.to_string()),
    }
}

#[uniffi::export]
fn get_file_type(path: String) -> TypedMetaResult {
    match file_type(path) {
        Ok(data) => TypedMetaResult::Ok(data),
        Err(e) => TypedMetaResult::Error(e.to_string()),
    }
}

#[uniffi::export]
fn get_file_types(path: String) -> TypedMetasResult {
    match file_types(path) {
        Ok(data) => TypedMetasResult::Ok(data),
        Err(e) => TypedMetasResult::Error(e.to_string()),
    }
}

#[uniffi::export]
fn get_mkfile(path: String) -> MetaResult {
    match new_file(path) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Error(e.to_string()),
    }
}

#[uniffi::export]
fn get_mkdir(path: String) -> MetaResult {
    match new_dir(path) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Error(e.to_string()),
    }
}
