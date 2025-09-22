use crate::bridge::Meta;
use crate::features::hr_meta::HumanReadableMeta;
use crate::features::meta_ext::MetaExt;
use crate::staff::Rslt;
use std::fs;
use std::fs::File;
use std::path::PathBuf;

pub fn meta(path: &String) -> Rslt<Meta> {
    let path = PathBuf::from(path);
    let meta = File::open(&path)?.metadata().to_hr(&path);
    return Ok(meta);
}

pub fn metas(path: &String) -> Rslt<Vec<Meta>> {
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
