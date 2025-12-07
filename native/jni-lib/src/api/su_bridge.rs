use crate::api::cancellation::CancellationState;
use crate::api::api::{SimpleResult, SuCmd};
use crate::api::su_api::{control_frame, from_control_frame, len_to_frame, ProgressProxy, Request, Response, FINAL_FRAME};
use crate::common::{config, Rslt};
use crate::ext::option::OptionExt;
use bincode::{decode_from_slice, encode_to_vec, Decode};
use once_cell::sync::Lazy;
use std::io;
use std::io::{Error, Read, Write};
use std::process::{Child, ExitStatus, Stdio};
use std::sync::{Arc, Mutex};

static CHILDREN: Lazy<Mutex<Vec<(Child, u32)>>> = Lazy::new(|| {
    Mutex::new(Vec::new())
});

#[uniffi::export]
fn try_as_su(su_cmd: SuCmd) -> SimpleResult {
    return as_su_impl::<(), SimpleResult>(Request::TryRun, su_cmd, Arc::new(()), None)
        .unwrap_or_else(|e| SimpleResult::Err(e.to_string()))
}

pub fn as_su<R: Decode<()>>(
    request: Request,
    su_cmd: SuCmd,
) -> Rslt<R> {
    as_su_impl::<(), R>(request, su_cmd, Arc::new(()), None)
}

pub fn as_su_with_progress<P: Decode<()>, R: Decode<()>>(
    request: Request,
    su_cmd: SuCmd,
    cancellation: Arc<dyn CancellationState>,
    collector: Box<dyn ProgressProxy<P>>,
) -> Rslt<R> {
    as_su_impl(request, su_cmd, cancellation, Some(collector))
}

fn as_su_impl<P: Decode<()>, R: Decode<()>>(
    request: Request,
    su_cmd: SuCmd,
    cancellation: Arc<dyn CancellationState>,
    collector: Option<Box<dyn ProgressProxy<P>>>,
) -> Rslt<R> {
    let (mut child, pid) = {
        CHILDREN.lock()?.pop()
    }.or_then(|| new_child(&su_cmd))?;

    let bytes = encode_to_vec(request, config())?;
    let stdin = child.stdin
        .as_mut().ok_or("failed to open stdin")?;
    let len_buf = len_to_frame(bytes.len());
    stdin.write_all(&len_buf)?;
    stdin.write_all(&bytes)?;
    stdin.flush()?;

    if let Some(collector) = collector {
        read_progress(&mut child, pid as i32, su_cmd, cancellation, collector)?;
    }

    let stdout = child.stdout
        .as_mut().ok_or("failed to open stdout")?;
    let mut len_buf = control_frame();
    let read_result = stdout.read_exact(&mut len_buf);
    if let Err(e) = read_result {
        return Err(get_error(&mut child, e))?;
    }
    let len = from_control_frame(len_buf) as usize;
    let mut bytes = vec![0u8; len];
    stdout.read_exact(&mut bytes)?;
    {
        CHILDREN.lock()?.push((child, pid))
    }
    let (response, _) = decode_from_slice::<Response,_>(&bytes, config())?;
    return match response {
        Response::Ok(bytes) => decode_from_slice::<R,_>(&bytes, config())
            .map(|(r,_)| r).map_err(|e| e.into()),
        Response::Err(e) => Err(e.into()),
    };
}

fn new_child(su_cmd: &SuCmd) -> Rslt<(Child, u32)> {
    let mut child = su_cmd.command(&su_cmd.bin_path)
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()?;
    let mut pid_bytes = control_frame();
    child.stdout.as_mut()
        .ok_or("failed to open stdin")?
        .read_exact(&mut pid_bytes)
        .expect("read pid failed");
    let pid = from_control_frame(pid_bytes);
    return Ok((child, pid));
}

fn read_progress<P>(
    child: &mut Child,
    pid: i32,
    su_cmd: SuCmd,
    cancellation: Arc<dyn CancellationState>,
    collector: Box<dyn ProgressProxy<P>>,
) -> Rslt<()> where P: Decode<()> {
    let mut stopped = false;
    let stdout = child.stdout
        .as_mut()
        .ok_or("failed to open stdout for progress")?;
    loop {
        let mut len_buf = control_frame();
        let read_result = stdout.read_exact(&mut len_buf);
        if let Err(e) = read_result {
            return Err(get_error(child, e))?;
        }
        if len_buf == FINAL_FRAME {
            return Ok(());
        }
        let len = from_control_frame(len_buf) as usize;
        let mut bytes = vec![0u8; len];
        stdout.read_exact(&mut bytes)?;
        let (progress, _) = decode_from_slice::<P,_>(&bytes, config())?;
        collector.emit(progress);
        if !stopped && cancellation.cancelled() {
            stopped = true;
            su_cmd.command(&format!("kill -SIGINT {pid}")).spawn()?;
            // don't return, read until get FINAL_FRAME
        }
    }
}

fn get_error(child: &mut Child, error: Error) -> String {
    let another = read_error(child)
        .unwrap_or_else(|e| e.to_string());
    return format!("{error}\n++++++++++++++++ {another}");
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
