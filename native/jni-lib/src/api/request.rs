use bincode::{Decode, Encode};

#[derive(Debug, Encode, Decode, PartialEq)]
pub enum Request {
    GetUsage(String),
    GetMeta(String),
    GetMetas(String),
    GetTypedMeta(String),
    GetTypedMetas(String),
    CreateDir(String),
    CreateFile(String),
    Delete(String),
    TryRun,
}

/*#[derive(Debug, Encode, Decode, PartialEq)]
pub enum ProxyResult {
    Ok(Vec<u8>),
    Error(String),
}
*/