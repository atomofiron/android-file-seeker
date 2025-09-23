use crate::bridge::{TypeEntries, TypeEntry};
use crate::features::hr_meta::HumanReadableMeta;
use crate::staff::{empty_string, Rslt};
use std::fs;
use std::path::PathBuf;

pub fn file_type(path: &String) -> Rslt<TypeEntry> {
    let path = PathBuf::from(path);
    let mime = tree_magic_mini::from_filepath(&path);
    let mut metadata = path.metadata();
    if mime == None {
        metadata = Ok(metadata?); // check both of them
    };
    let entry = TypeEntry {
        meta: Some(metadata.to_hr(&path)),
        mime: mime.map(|m| m.to_string()).unwrap_or(empty_string()),
    };
    return Ok(entry)
}

pub fn file_types(path: &String) -> Rslt<TypeEntries> {
    let dir = fs::read_dir(path)?;
    let entries = dir.filter_map(|entry| {
        entry.ok().map(|e| e.path()).and_then(|path| {
            let meta = path.metadata().to_hr(&path);
            tree_magic_mini::from_filepath(&PathBuf::from(path))
                .map(|mime| TypeEntry { meta: Some(meta), mime: mime.to_string() })
        })
    }).collect::<Vec<_>>();
    return Ok(TypeEntries { entries });
}
