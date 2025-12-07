use crate::api::api::Meta;
use crate::common::{empty_string, DATE, DATE_STUB, TIME, TIME_STUB};
use crate::ext::raw_path::PathExt;
use crate::r#impl::fs_mode::HumanReadableMode;
use crate::r#impl::hr_size::HumanReadableSize;
use crate::r#impl::hr_users::HumanReadableUsers;
use chrono::{DateTime, Local};
use std::fs::Metadata;
use std::io;
use std::os::unix::fs::MetadataExt;
use std::path::PathBuf;

pub trait HumanReadableMeta {
    fn to_hr(self, name: &PathBuf) -> Meta;
}

impl HumanReadableMeta for io::Result<Metadata> {

    fn to_hr(self, path: &PathBuf) -> Meta {
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
                    path: path.clone().raw(),
                    length: meta.size(),
                    error: None,
                }
            },
            Err(e) => Meta::with_error(path, &e),
        }
    }
}
