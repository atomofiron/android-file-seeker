use crate::api::protocol::Meta;

pub trait MetaExt {
    fn is_dir(&self) -> bool;
}

impl MetaExt for Meta {
    fn is_dir(&self) -> bool {
        self.access.chars().nth(0) == Some('d')
    }
}