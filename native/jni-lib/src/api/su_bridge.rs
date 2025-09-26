use crate::api::protocol::SimpleResult;
use crate::api::su_protocol::{from_len_frame, to_len_frame, Request};
use crate::common::config;
use crate::ext::option::OptionExt;
use crate::ext::result::Rslt;
use bincode::{decode_from_slice, encode_to_vec, Decode};
use once_cell::sync::Lazy;
use std::io;
use std::io::{Read, Write};
use std::process::{Child, Command, Stdio};
use std::sync::Mutex;

static CHILDREN: Lazy<Mutex<Vec<Child>>> = Lazy::new(|| {
    Mutex::new(Vec::new())
});

#[uniffi::export]
fn try_as_su(bin_path: String) -> SimpleResult {
    return as_su::<SimpleResult>(Request::TryRun, bin_path)
        .unwrap_or_else(|e| SimpleResult::Error(e.to_string()))
}

pub fn as_su<D: Decode<()>>(request: Request, bin_path: String) -> Rslt<D> {
    let mut child = {
        CHILDREN.lock().unwrap().pop()
    }.or_then(|| new_child(bin_path))?;

    let bytes = encode_to_vec(request, config())?;
    let stdin = child.stdin
        .as_mut().ok_or("failed to open stdin")?;
    let mut len_buf = to_len_frame(bytes.len());
    stdin.write_all(&len_buf)?;
    stdin.write_all(&bytes)?;
    stdin.flush()?;

    let stdout = child.stdout
        .as_mut().ok_or("failed to open stdout")?;
    stdout.read_exact(&mut len_buf)?;
    let len = from_len_frame(len_buf);
    let mut bytes = vec![0u8; len];
    stdout.read_exact(&mut bytes)?;
    {
        CHILDREN.lock().unwrap().push(child)
    }
    let (response, _) = decode_from_slice::<D,_>(&bytes, config())?;
    return Ok(response)
}

fn new_child(bin_path: String) -> io::Result<Child> {
    Command::new("su").arg("-c").arg(bin_path)
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()
}
