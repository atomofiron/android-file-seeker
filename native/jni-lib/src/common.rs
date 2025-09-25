use bincode::config::Configuration;
use std::error::Error;

pub type Rslt<T> = Result<T, Box<dyn Error>>;

pub fn config() -> Configuration { bincode::config::standard() }

pub fn empty_string() -> String { String::new() }

pub const DATE: &str = "%Y-%m-%d";
pub const TIME: &str = "%H:%M:%S";
pub const DATE_STUB: &str = "????-??-??";
pub const TIME_STUB: &str = "??:??:??";
