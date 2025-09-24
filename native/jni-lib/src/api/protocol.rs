
#[derive(uniffi::Enum)]
pub enum MetaResult {
    Ok(Meta),
    Error(String),
}

#[derive(uniffi::Enum)]
pub enum MetasResult {
    Ok(Vec<Meta>),
    Error(String),
}

#[derive(uniffi::Enum)]
pub enum TypedMetaResult {
    Ok(TypedMeta),
    Error(String),
}

#[derive(uniffi::Enum)]
pub enum TypedMetasResult {
    Ok(Vec<TypedMeta>),
    Error(String),
}

#[derive(uniffi::Enum)]
pub enum UsageResult {
    Ok(String),
    Error(String),
}

#[derive(uniffi::Enum)]
pub enum DeleteResult {
    Ok,
    Error(String),
}

#[derive(uniffi::Record)]
pub struct Meta {
    pub name: String,
    pub access: String,
    pub owner: String,
    pub group: String,
    pub length: u64,
    pub size: String,
    pub date: String,
    pub time: String,
    pub error: String,
}

#[derive(uniffi::Record)]
pub struct TypedMeta {
    pub meta: Meta,
    pub mime: String,
}
