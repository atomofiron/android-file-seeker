use crate::protocol;
use crate::features::fs_mode::HumanReadableMode;
use crate::features::hr_size::HumanReadableSize;
use crate::features::hr_users::HumanReadableUsers;
use protocol::Meta;
use std::fs::Metadata;
use std::io;
use std::os::unix::fs::MetadataExt;
use std::path::PathBuf;
use chrono::{DateTime, Local};
use crate::staff::{empty_string, DATE, DATE_STUB, TIME, TIME_STUB};

pub trait HumanReadableMeta {
    fn to_hr(self, name: &PathBuf) -> Meta;
}

impl HumanReadableMeta for io::Result<Metadata> {
    fn to_hr(self, path: &PathBuf) -> Meta {
        let name = path.file_name().unwrap().to_str().unwrap().to_owned();
        match self {
            Ok(meta) => {
                let date_time = DateTime::from_timestamp(meta.mtime(), 0)
                    .map(|it| it.with_timezone(&Local));
                let date = date_time.map(|it| it.format(DATE).to_string())
                    .unwrap_or(DATE_STUB.to_string());
                let time = date_time.map(|it| it.format(TIME).to_string())
                    .unwrap_or(TIME_STUB.to_string());
                let size = match meta.is_file() {
                    true => meta.size().to_hr_size(),
                    false => empty_string(),
                };
                Meta {
                    access: meta.mode().to_hr_mode(),
                    owner: meta.to_hr_owner(),
                    group: meta.to_hr_group(),
                    size,
                    date,
                    time,
                    name,
                    length: meta.size(),
                    error: empty_string(),
                }
            },
            Err(e) => Meta {
                access: empty_string(),
                owner: empty_string(),
                group: empty_string(),
                size: empty_string(),
                date: empty_string(),
                time: empty_string(),
                name,
                length: 0,
                error: e.to_string(),
            }
        }
    }
}
