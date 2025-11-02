use crate::ext::raw_path::RawPath;
use bincode::{Decode, Encode};

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
pub struct SearchQuery {
    pub query: String,
    pub regex: bool,
    pub case_insensitive: bool,
}

#[derive(Debug, Encode, Decode, PartialEq)]
#[derive(uniffi::Enum)]
pub enum NameSearchProgress {
    Ok(TypedMeta),
    Skip,
    Err(Meta),
}

#[derive(Debug, Encode, Decode, PartialEq)]
#[derive(uniffi::Enum)]
pub enum TextSearchProgress {
    Ok {
        path: RawPath,
        offset: u64,
        length: u32,
        line: Option<u64>,
    },
    End(RawPath),
    Err(Meta),
}

#[derive(Debug, Encode, Decode, PartialEq)]
#[derive(uniffi::Enum)]
pub enum Check {
    Yes(u64), // txt + file size
    No,
}

#[derive(Debug, Encode, Decode, PartialEq, Clone)]
#[derive(uniffi::Record)]
pub struct CommonProgress {
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
    pub error: Option<String>,
}

#[derive(Debug, Encode, Decode, PartialEq)]
#[derive(uniffi::Record)]
pub struct TypedMeta {
    pub meta: Meta,
    pub mime: String,
}

#[uniffi::export(with_foreign)]
pub trait CommonProgressCollector: Send + Sync {
    fn emit(&self, progress: CommonProgress) -> bool;
}

impl CommonProgressCollector for () {
    fn emit(&self, _: CommonProgress) -> bool { true }
}

#[uniffi::export(with_foreign)]
pub trait NameSearchCollector: Send + Sync {
    fn emit(&self, progress: NameSearchProgress) -> bool;
}

#[uniffi::export(with_foreign)]
pub trait TextSearchCollector: Send + Sync {
    fn emit(&self, progress: TextSearchProgress) -> bool;
}
