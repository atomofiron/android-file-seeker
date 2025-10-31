use crate::api::protocol::Meta;
use crate::common::empty_string;
use std::fmt::Display;
use std::path::PathBuf;
use crate::ext::raw_path::RawPathExt;

pub trait MetaExt {
    fn is_dir(&self) -> bool;
}

impl MetaExt for Meta {

    fn is_dir(&self) -> bool {
        self.access.chars().nth(0) == Some('d')
    }
}

impl Meta {

    pub fn with_error(path: &PathBuf, error: &impl Display) -> Meta {
        Meta {
            access: empty_string(),
            owner: empty_string(),
            group: empty_string(),
            size: empty_string(),
            date: empty_string(),
            time: empty_string(),
            path: path.clone().raw(),
            length: 0,
            error: Some(error.to_string()),
        }
    }
}
