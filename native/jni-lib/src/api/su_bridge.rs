use crate::api::protocol::SimpleResult;
use crate::api::su_protocol::{frame_length, from_len_frame, to_len_frame, ProgressProxy, Request, Response, FINAL_FRAME};
use crate::common::{config, Rslt};
use crate::ext::option::OptionExt;
use crate::ext::result::ResultExt;
use bincode::{decode_from_slice, encode_to_vec, Decode};
use once_cell::sync::Lazy;
use std::io;
use std::io::{Error, Read, Write};
use std::process::{Child, Command, ExitStatus, Stdio};
use std::sync::Mutex;

static CHILDREN: Lazy<Mutex<Vec<Child>>> = Lazy::new(|| {
    Mutex::new(Vec::new())
});

#[uniffi::export]
fn try_as_su(bin_path: String) -> SimpleResult {
    return as_su_impl::<(), SimpleResult>(Request::TryRun, bin_path, None)
        .unwrap_or_else(|e| SimpleResult::Err(e.to_string()))
}

pub fn as_su<R: Decode<()>>(request: Request, bin_path: String) -> Rslt<R> {
    as_su_impl::<(), R>(request, bin_path, None)
}

pub fn as_su_with_progress<P: Decode<()>, R: Decode<()>>(
    request: Request,
    bin_path: String,
    collector: Box<dyn ProgressProxy<P>>,
) -> Rslt<R> {
    as_su_impl(request, bin_path, Some(collector))
}

fn as_su_impl<P: Decode<()>, R: Decode<()>>(
    request: Request,
    bin_path: String,
    collector: Option<Box<dyn ProgressProxy<P>>>,
) -> Rslt<R> {
    let mut child = {
        CHILDREN.lock()?.pop()
    }.or_then(|| new_child(bin_path))?;

    let bytes = encode_to_vec(request, config())?;
    let stdin = child.stdin
        .as_mut().ok_or("failed to open stdin")?;
    let len_buf = to_len_frame(bytes.len());
    stdin.write_all(&len_buf)?;
    stdin.write_all(&bytes)?;
    stdin.flush()?;

    if let Some(collector) = collector {
        read_progress(&mut child, collector)?;
    }

    let stdout = child.stdout
        .as_mut().ok_or("failed to open stdout")?;
    let mut len_buf = frame_length();
    let read_result = stdout.read_exact(&mut len_buf);
    if let Err(e) = read_result {
        return Err(get_error(&mut child, e))?;
    }
    let len = from_len_frame(len_buf);
    let mut bytes = vec![0u8; len];
    stdout.read_exact(&mut bytes)?;
    {
        CHILDREN.lock()?.push(child)
    }
    let (response, _) = decode_from_slice::<Response,_>(&bytes, config())?;
    return match response {
        Response::Ok(bytes) => decode_from_slice::<R,_>(&bytes, config())
            .map(|(r,_)| r).boxed(),
        Response::Err(e) => Err(e.into()),
    };
}

fn new_child(bin_path: String) -> io::Result<Child> {
    Command::new("su").arg("-c").arg(bin_path)
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()
}

fn read_progress<P>(child: &mut Child, collector: Box<dyn ProgressProxy<P>>) -> Rslt<()> where P: Decode<()> {
    let stdout = child.stdout
        .as_mut().ok_or("failed to open stdout for progress")?;
    loop {
        let mut len_buf = frame_length();
        let read_result = stdout.read_exact(&mut len_buf);
        if let Err(e) = read_result {
            return Err(get_error(child, e))?;
        }
        let len = from_len_frame(len_buf);
        if len == FINAL_FRAME {
            return Ok(());
        }
        let mut bytes = vec![0u8; len];
        stdout.read_exact(&mut bytes)?;
        let (progress, _) = decode_from_slice::<P,_>(&bytes, config())?;
        collector.emit(progress)
    }
}

fn get_error(child: &mut Child, error: Error) -> String {
    let another = read_error(child)
        .unwrap_or_else(|e| e.to_string());
    return format!("{error}\n++++++++++++++++{another}");
}

fn read_error(child: &mut Child) -> Rslt<String> {
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
