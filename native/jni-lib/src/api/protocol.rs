use bincode::{Decode, Encode};
use crate::ext::raw_path::RawPath;

#[derive(Debug, Encode, Decode, PartialEq)]
#[derive(uniffi::Enum)]
pub enum MetaResult {
    Ok(Meta),
    Err(String),
}

#[derive(Debug, Encode, Decode, PartialEq)]
#[derive(uniffi::Enum)]
pub enum MetasResult {
    Ok(Vec<Meta>),
    Err(String),
}

#[derive(Debug, Encode, Decode, PartialEq)]
#[derive(uniffi::Enum)]
pub enum TypedMetaResult {
    Ok(TypedMeta),
    Err(String),
}

#[derive(Debug, Encode, Decode, PartialEq)]
#[derive(uniffi::Enum)]
pub enum TypedMetasResult {
    Ok(Vec<TypedMeta>),
    Err(String),
}

#[derive(Debug, Encode, Decode, PartialEq)]
#[derive(uniffi::Enum)]
pub enum UsageResult {
    Ok(String),
    Err(String),
}

#[derive(Debug, Encode, Decode, PartialEq)]
#[derive(uniffi::Enum)]
pub enum SimpleResult {
    Ok,
    Err(String),
}

#[derive(Debug, Encode, Decode, PartialEq)]
#[derive(uniffi::Enum)]
pub enum ComplexResult {
    Ok {
        count: u32,
        errors: Vec<String>,
        meta: Option<Meta>,
    },
    Err(Meta),
}

#[derive(Debug, Encode, Decode, PartialEq, Clone)]
#[derive(uniffi::Record)]
pub struct Progress {
    pub count: u32,
    pub errors: u32,
    pub progress: f32,
}

#[derive(Debug, Encode, Decode, PartialEq, Clone)]
#[derive(uniffi::Record)]
pub struct Meta {
    pub path: RawPath,
    pub access: String,
    pub owner: String,
    pub group: String,
    pub length: u64,
    pub size: String,
    pub date: String,
    pub time: String,
    pub error: String,
}

#[derive(Debug, Encode, Decode, PartialEq)]
#[derive(uniffi::Record)]
pub struct TypedMeta {
    pub meta: Meta,
    pub mime: String,
}

#[uniffi::export(with_foreign)]
pub trait ProgressCollector: Send + Sync {
    fn emit(&self, progress: Progress);
}

impl ProgressCollector for () {
    fn emit(&self, _: Progress) { }
}
