use crate::api::protocol::Meta;
use crate::common::Rslt;
use crate::kopy;
use crate::r#impl::hr_meta::HumanReadableMeta;
use crate::r#impl::meta_ext::MetaExt;
use std::fmt::Display;
use std::fs;
use std::fs::File;
use std::path::PathBuf;

pub fn meta(path: &PathBuf) -> Rslt<Meta> {
    let meta = File::open(path)?.metadata();
    return Ok(meta.to_hr(path));
}

pub fn meta_with_error(path: &PathBuf, error: &impl Display) -> Meta {
    let meta = File::open(path)
        .and_then(|f| f.metadata())
        .to_hr(path);
    return kopy!(meta, error = Some(error.to_string()));
}

pub fn metas(path: &PathBuf) -> Rslt<Vec<Meta>> {
    let dir = fs::read_dir(path)?;
    let mut entries: Vec<_> = dir.filter_map(|entry| {
        match entry {
            Ok(entry) => Some(entry.path().metadata().to_hr(&entry.path())),
            Err(_) => None,
        }
    }).collect();
    entries.sort_by_key(|entry| !entry.is_dir());
    return Ok(entries);
}
