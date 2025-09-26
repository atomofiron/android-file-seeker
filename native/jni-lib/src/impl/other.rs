use crate::api::protocol::Meta;
use crate::ext::result::Rslt;
use crate::r#impl::hr_meta::HumanReadableMeta;
use crate::r#impl::hr_size::HumanReadableSize;
use fs_extra::dir;
use std::fs;
use std::fs::File;
use std::path::PathBuf;

pub fn new_file(path: String) -> Rslt<Meta> {
    let path = PathBuf::try_from(&path)?;
    let meta = File::create(&path)?.metadata();
    return Ok(meta.to_hr(&path));
}

pub fn new_dir(path: String) -> Rslt<Meta> {
    fs::create_dir_all(path.clone())?;
    let path = PathBuf::try_from(&path)?;
    let meta = File::open(&path)?.metadata();
    return Ok(meta.to_hr(&path));
}

pub fn delete(path: String) -> Rslt<()> {
    let path = PathBuf::try_from(&path)?;
    match path.is_dir() {
        true => fs::remove_dir_all(path)?,
        false => fs::remove_file(path)?,
    }
    return Ok(());
}

pub fn usage(path: String) -> Rslt<String> {
    Ok(dir::get_size(path)?.to_hr_size())
}
