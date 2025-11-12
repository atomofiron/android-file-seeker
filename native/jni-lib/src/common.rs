use std::error::Error;
use bincode::config::Configuration;
use libc::{c_int, c_long, ssize_t};

pub type Rslt<T> = Result<T, Box<dyn Error>>;

pub const OKI: c_int = 0;
pub const OKL: c_long = 0;
pub const OKS: ssize_t = 0;

pub fn config() -> Configuration { bincode::config::standard() }

pub fn empty_string() -> String { String::new() }

pub const DATE: &str = "%Y-%m-%d";
pub const TIME: &str = "%H:%M:%S";
pub const DATE_STUB: &str = "????-??-??";
pub const TIME_STUB: &str = "??:??:??";

pub const JOINING_ERROR: &str = "Joining thread failed";
pub const EMPTY_VALUE_ERROR: &str = "Value is empty";
pub const PERMISSION_DENIED: &str = "Permission denied";
pub const RESOURCE_BUSY: &str = "Device or resource busy";
