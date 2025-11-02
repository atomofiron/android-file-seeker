use crate::api::protocol::TypedMeta;
use crate::common::{empty_string, Rslt};
use crate::r#impl::hr_meta::HumanReadableMeta;
use std::fs;
use std::path::PathBuf;
use crate::r#impl::meta::meta_with_error;

pub fn file_type(path: &PathBuf) -> Rslt<TypedMeta> {
    let mime = tree_magic_mini::from_filepath(path);
    let mut metadata = path.metadata();
    if mime == None {
        metadata = Ok(metadata?); // check both of them
    };
    let entry = TypedMeta {
        meta: metadata.to_hr(path),
        mime: mime.map(|m| m.to_string()).unwrap_or(empty_string()),
    };
    return Ok(entry)
}

pub fn file_type_or_error(path: &PathBuf) -> TypedMeta {
    match file_type(path) {
        Ok(file_type) => file_type,
        Err(e) => TypedMeta {
            meta: meta_with_error(path, &e),
            mime: empty_string(),
        },
    }
}

pub fn file_types(path: &PathBuf) -> Rslt<Vec<TypedMeta>> {
    let dir = fs::read_dir(path)?;
    let entries = dir.filter_map(|entry| {
        entry.ok().map(|e| e.path()).and_then(|path| {
            let meta = path.metadata().to_hr(&path);
            tree_magic_mini::from_filepath(&path)
                .map(|mime| TypedMeta { meta, mime: mime.to_string() })
        })
    }).collect::<Vec<_>>();
    return Ok(entries);
}
