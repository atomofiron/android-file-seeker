use std::error::Error;
use crate::bridge::{Meta, TypeEntry};

pub type Rslt<T> = Result<T, Box<dyn Error>>;

pub fn empty_string() -> String { String::new() }

pub const DATE: &str = "%Y-%m-%d";
pub const TIME: &str = "%H:%M:%S";
pub const DATE_STUB: &str = "????-??-??";
pub const TIME_STUB: &str = "??:??:??";

impl TypeEntry {
    pub fn meta_or_default(&self) -> Meta {
        self.meta.clone().unwrap_or_default()
    }
}