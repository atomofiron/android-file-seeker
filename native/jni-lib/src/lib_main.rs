use crate::api::bridge::{copy, create_dir, create_file, delete_by, find_names, find_text, get_file_type, get_file_types, get_meta, get_metas, get_usage};
use crate::api::cancellation::{CancellationHandle, CancellationState};
use crate::api::protocol::{CommonProgress, CommonProgressCollector, NameSearchCollector, NameSearchProgress, SimpleResult, TextSearchCollector, TextSearchProgress};
use crate::api::su_protocol::{control_frame, from_control_frame, len_to_frame, pid_to_frame, Request, Response, FINAL_FRAME};
use crate::common::{config, Rslt};
use crate::ext::encode::{EncodeExt, EncodedResult};
use crate::ext::result::ResultExt;
use bincode::{decode_from_slice, enc, encode_to_vec};
use std::io::{stdin, stdout, Read, Write};
use std::sync::Arc;

#[no_mangle]
pub extern "C" fn lib_main() {
    let pid_frame = pid_to_frame(std::process::id());
    stdout().write_all(&pid_frame)
        .expect("write pid failed");
    stdout().flush()
        .expect("flush pid failed");
    let cancellation = Arc::new(CancellationHandle::new());
    let canceller = cancellation.clone();
    ctrlc::set_handler(move || canceller.cancel())
        .expect("error setting SIGINT handler");
    loop {
        let result = get_request()
            .and_then(|r| run(r, cancellation.clone()).boxed());
        cancellation.reset();
        let response = match result {
            Ok(bytes) => Response::Ok(bytes),
            Err(e) => Response::Err(e.to_string()),
        };
        write_response(response);
    }
}

fn get_request() -> Rslt<Request> {
    let mut stdin = stdin();
    let mut len_buf = control_frame();
    stdin.read_exact(&mut len_buf)?;
    let len = from_control_frame(len_buf) as usize;
    let mut bytes = vec![0u8; len];
    stdin.read_exact(&mut bytes)?;
    return decode_from_slice::<Request, _>(&bytes, config())
        .map(|(r,_)| r).boxed()
}

fn run(request: Request, cancellation: Arc<dyn CancellationState>) -> EncodedResult {
    match request {
        Request::TryRun => SimpleResult::Ok.to_bytes(),
        Request::GetUsage(arg) => get_usage(arg, None).to_bytes(),
        Request::GetMeta(arg) => get_meta(arg, None).to_bytes(),
        Request::GetMetas(arg) => get_metas(arg, None).to_bytes(),
        Request::GetTypedMeta(arg) => get_file_type(arg, None).to_bytes(),
        Request::GetTypedMetas(arg) => get_file_types(arg, None).to_bytes(),
        Request::CreateDir(arg) => create_dir(arg, None).to_bytes(),
        Request::CreateFile(arg) => create_file(arg, None).to_bytes(),
        Request::Delete(arg) => {
            let result = delete_by(arg, None, StdoutProgressWriter::arc());
            write_the_end();
            result.to_bytes()
        },
        Request::Copy(from, to, moving) => {
            let result = copy(from, to, moving, None, StdoutProgressWriter::arc());
            write_the_end();
            result.to_bytes()
        },
        Request::FindNames { query, targets, max_depth, exclude_dirs: exclude_dir } => {
            let result = find_names(query, targets, max_depth, exclude_dir, None, cancellation, StdoutProgressWriter::arc());
            write_the_end();
            result.to_bytes()
        }
        Request::FindText { query, targets, max_depth, check } => {
            let result = find_text(query, targets, max_depth, check, None, cancellation, StdoutProgressWriter::arc());
            write_the_end();
            result.to_bytes()
        }
    }
}

fn write_response<E>(response: E) where E: enc::Encode {
    let bytes = encode_to_vec(response, config())
        .expect("encode response failed");
    let mut stdout = stdout();
    let len_buf = len_to_frame(bytes.len());
    stdout.write_all(&len_buf)
        .expect("write len failed");
    stdout.write_all(&bytes)
        .expect("write bytes failed");
    stdout.flush()
        .expect("flush response failed");
}

fn write_the_end() {
    let mut stdout = stdout();
    stdout.write_all(&FINAL_FRAME)
        .expect("write final failed");
    stdout.flush()
        .expect("flush final frame failed");
}

struct StdoutProgressWriter;

impl StdoutProgressWriter {

    pub fn arc() -> Arc<Self> {
        Arc::new(StdoutProgressWriter { })
    }
}

impl <'l>CommonProgressCollector for StdoutProgressWriter {

    fn emit(&self, progress: CommonProgress) {
        write_response(progress);
    }
}

impl <'l>NameSearchCollector for StdoutProgressWriter {

    fn emit(&self, progress: NameSearchProgress) {
        write_response(progress);
    }
}

impl <'l>TextSearchCollector for StdoutProgressWriter {

    fn emit(&self, progress: TextSearchProgress) {
        write_response(progress);
    }
}
