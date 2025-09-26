use crate::api::protocol::{SimpleResult, MetaResult, MetasResult, TypedMetaResult, TypedMetasResult, UsageResult};
use crate::api::request::Request;
use crate::common::{config, Rslt};
use crate::r#impl::meta::{meta, metas};
use crate::r#impl::other::{delete, new_dir, new_file, usage};
use crate::r#impl::r#type::{file_type, file_types};
use bincode::{decode_from_slice, encode_to_vec, Decode};
use std::io::{Read, Write};
use std::process::{ChildStderr, ChildStdout, Command, Stdio};

#[uniffi::export]
pub fn create_file(path: String, run_as_su: Option<String>) -> MetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<MetaResult>(Request::GetMeta(path), bin_path)
            .unwrap_or_else(|e| MetaResult::Error(e.to_string()))
    }
    match new_file(path) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Error(e.to_string()),
    }
}

#[uniffi::export]
pub fn create_dir(path: String, run_as_su: Option<String>) -> MetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<MetaResult>(Request::GetMeta(path), bin_path)
            .unwrap_or_else(|e| MetaResult::Error(e.to_string()))
    }
    match new_dir(path) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Error(e.to_string()),
    }
}

#[uniffi::export]
pub fn delete_by(path: String, run_as_su: Option<String>) -> SimpleResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<SimpleResult>(Request::Delete(path), bin_path)
            .unwrap_or_else(|e| SimpleResult::Error(e.to_string()))
    }
    match delete(path) {
        Ok(_) => SimpleResult::Ok,
        Err(e) => SimpleResult::Error(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_usage(path: String, run_as_su: Option<String>) -> UsageResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<UsageResult>(Request::GetUsage(path), bin_path)
            .unwrap_or_else(|e| UsageResult::Error(e.to_string()))
    }
    match usage(path) {
        Ok(data) => UsageResult::Ok(data),
        Err(e) => UsageResult::Error(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_meta(path: String, run_as_su: Option<String>) -> MetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<MetaResult>(Request::GetMeta(path), bin_path)
            .unwrap_or_else(|e| MetaResult::Error(e.to_string()))
    }
    match meta(path) {
        Ok(data) => MetaResult::Ok(data),
        Err(e) => MetaResult::Error(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_metas(path: String, run_as_su: Option<String>) -> MetasResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<MetasResult>(Request::GetMetas(path), bin_path)
            .unwrap_or_else(|e| MetasResult::Error(e.to_string()))
    }
    match metas(path) {
        Ok(data) => MetasResult::Ok(data),
        Err(e) => MetasResult::Error(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_file_type(path: String, run_as_su: Option<String>) -> TypedMetaResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<TypedMetaResult>(Request::GetTypedMeta(path), bin_path)
            .unwrap_or_else(|e| TypedMetaResult::Error(e.to_string()))
    }
    match file_type(path) {
        Ok(data) => TypedMetaResult::Ok(data),
        Err(e) => TypedMetaResult::Error(e.to_string()),
    }
}

#[uniffi::export]
pub fn get_file_types(path: String, run_as_su: Option<String>) -> TypedMetasResult {
    if let Some(bin_path) = run_as_su {
        return as_su::<TypedMetasResult>(Request::GetTypedMetas(path), bin_path)
            .unwrap_or_else(|e| TypedMetasResult::Error(e.to_string()))
    }
    match file_types(path) {
        Ok(data) => TypedMetasResult::Ok(data),
        Err(e) => TypedMetasResult::Error(e.to_string()),
    }
}

#[uniffi::export]
fn try_as_su(bin_path: String) -> SimpleResult {
    return as_su::<SimpleResult>(Request::TryRun, bin_path)
        .unwrap_or_else(|e| SimpleResult::Error(e.to_string()))
}

fn as_su<D: Decode<()>>(request: Request, bin_path: String) -> Rslt<D> {
    let bytes = encode_to_vec(request, config())?;
    let mut child = Command::new("su").arg("-c").arg(bin_path)
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()?;
    let mut stdin = child.stdin.take()
        .ok_or("failed to open stdin")?;
    stdin.write_all(&bytes)?;
    drop(stdin); // EOF
    let status = child.wait()?;
    if status.success() {
        let (response, _) = decode_from_slice::<D,_>(&get_out(child.stdout)?, config())?;
        return Ok(response)
    } else {
        let err = get_err(child.stderr)?;
        let code = status.code()
            .map(|x| x.to_string())
            .unwrap_or("error".to_string());
        Err(format!("{code}: {err}"))?
    }
}
fn get_out(mut stdout: Option<ChildStdout>) -> Rslt<Vec<u8>> {
    let mut out = Vec::new();
    stdout
        .as_mut().ok_or("stdout.as_mut")?
        .read_to_end(&mut out)?;
    return Ok(out);
}
fn get_err(mut stderr: Option<ChildStderr>) -> Rslt<String> {
    let mut out = Vec::new();
    stderr
        .as_mut().ok_or("stderr.as_mut")?
        .read_to_end(&mut out)?;
    let message = String::from_utf8_lossy(&out)
        .into_owned().to_string();
    return Ok(message);
}
