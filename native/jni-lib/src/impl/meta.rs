use crate::api::protocol::Meta;
use crate::r#impl::hr_meta::HumanReadableMeta;
use crate::r#impl::meta_ext::MetaExt;
use std::fs;
use std::fs::File;
use std::path::PathBuf;
use crate::common::Rslt;

pub fn meta(path: String) -> Rslt<Meta> {
    let path = PathBuf::from(&path);
    let meta = File::open(&path)?.metadata();
    return Ok(meta.to_hr(&path));
}

pub fn metas(path: String) -> Rslt<Vec<Meta>> {
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
