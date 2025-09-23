use crate::bridge;
use crate::features::hr_meta::HumanReadableMeta;
use crate::staff::Rslt;
use std::fs::File;
use std::path::PathBuf;
use fs_extra::dir;
use crate::features::hr_size::HumanReadableSize;

pub fn mkfile(path: &String) -> Rslt<bridge::Meta> {
    let path = PathBuf::try_from(path)?;
    let meta = File::create(&path)?.metadata();
    return Ok(meta.to_hr(&path));
}

pub fn mkdir(path: &String) -> Rslt<bridge::Meta> {
    std::fs::create_dir_all(path.clone())?;
    let path = PathBuf::try_from(path)?;
    let meta = File::open(&path)?.metadata();
    return Ok(meta.to_hr(&path));
}

pub fn delete(path: &String) -> bool {
    std::fs::remove_dir_all(path).is_ok()
}

pub fn usage(path: &String) -> Rslt<String> {
    Ok(dir::get_size(path)?.to_hr_size())
}
