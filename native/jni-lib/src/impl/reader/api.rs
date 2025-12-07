use bincode::{Decode, Encode};
use std::fs::File;
use std::io::BufReader;
use std::process::Child;
use std::sync::{Arc, Mutex};

pub enum TextProvider {
    Direct(BufReader<File>),
    Child(Child, u32),
}

#[derive(uniffi::Object)]
pub struct FileReader {
    pub provider: Mutex<TextProvider>,
}

#[derive(uniffi::Enum)]
pub enum ReaderResult {
    Ok(Arc<FileReader>),
    Err(String),
}

#[derive(uniffi::Enum)]
#[derive(Debug, Encode, Decode, PartialEq)]
pub enum ReadResult {
    Ok(Vec<u8>),
    End,
    Err(String), // Box<dyn Error> ?
}

#[uniffi::export]
impl FileReader {

    pub fn next(&self) -> ReadResult {
        self.try_next()
            .unwrap_or_else(|e| ReadResult::Err(e.to_string()))
    }
}
