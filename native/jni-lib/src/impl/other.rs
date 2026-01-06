use crate::api::api::Meta;
use crate::common::{Rslt, OKI};
use crate::ext::result::ResultExt;
use crate::r#impl::hr_meta::HumanReadableMeta;
use crate::r#impl::hr_size::HumanReadableSize;
use fs_extra::dir;
use libc::c_int;
use std::fs::File;
use std::io::Read;
use std::path::PathBuf;
use std::process::{Child, ExitStatus};
use std::{fs, io};

pub fn last_os_error<T>() -> Rslt<T> {
    Err(io::Error::last_os_error().into())
}

pub fn raw_os_error() -> c_int {
    io::Error::last_os_error().raw_os_error().unwrap_or(OKI)
}

pub fn new_file(path: &PathBuf) -> Rslt<Meta> {
    let meta = File::create(path)?.metadata();
    return Ok(meta.to_hr(path));
}

pub fn new_dir(path: &PathBuf) -> Rslt<Meta> {
    fs::create_dir_all(path.clone())?;
    let meta = File::open(path)?.metadata();
    return Ok(meta.to_hr(path));
}

pub fn usage(path: &PathBuf) -> Rslt<(u64, String)> {
    dir::get_size(path)
        .map(|r| (r, r.to_hr_size()))
        .boxed()
}

pub fn read_error(child: &mut Child, error: io::Error) -> String {
    let another = try_read_error(child)
        .unwrap_or_else(|e| e.to_string());
    return format!("{error}\n++++++++++++++++ {another}");
}

fn try_read_error(child: &mut Child) -> Rslt<String> {
    let stderr = child.stderr.as_mut().ok_or("_");
    return match stderr {
        Ok(stderr) => {
            let mut message = String::new();
            stderr.read_to_string(&mut message)
                .map(|_| message.as_str().into())
                .map_err(Into::into)
        }
        Err(_) => {
            let code = get_exit_code(child.try_wait())
                .unwrap_or_else(|e| e.to_string());
            Ok(format!("code: {code}"))
        },
    };
}

fn get_exit_code(status: io::Result<Option<ExitStatus>>) -> Rslt<String> {
    status?
        .and_then(|it| it.code())
        .map(|it| it.to_string())
        .ok_or_else(|| "null".into())
}
