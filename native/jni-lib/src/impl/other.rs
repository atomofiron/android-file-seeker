use crate::api::protocol::Meta;
use crate::common::Rslt;
use crate::ext::result::ResultExt;
use crate::r#impl::hr_meta::HumanReadableMeta;
use crate::r#impl::hr_size::HumanReadableSize;
use fs_extra::dir;
use std::fs;
use std::fs::File;
use std::path::PathBuf;

pub fn new_file(path: &PathBuf) -> Rslt<Meta> {
    let meta = File::create(path)?.metadata();
    return Ok(meta.to_hr(path));
}

pub fn new_dir(path: &PathBuf) -> Rslt<Meta> {
    fs::create_dir_all(path.clone())?;
    let meta = File::open(&path)?.metadata();
    return Ok(meta.to_hr(&path));
}

pub fn usage(path: &PathBuf) -> Rslt<String> {
    dir::get_size(path)
        .map(|r| r.to_hr_size())
        .boxed()
}
