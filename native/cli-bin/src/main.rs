use bincode::{decode_from_slice, encode_to_vec};
use native_lib::api::bridge::{create_dir, create_file, delete_by, get_file_type, get_file_types, get_meta, get_metas, get_usage};
use native_lib::api::protocol::SimpleResult;
use native_lib::api::su_protocol::{frame_length, from_len_frame, to_len_frame, Request, Response};
use native_lib::common::{config, Rslt};
use native_lib::ext::result::ResultExt;
use std::io::{stdin, stdout, Read, Write};

fn main() {
    loop {
        let result = get_request()
            .and_then(|r| run(r));
        let response = match result {
            Ok(bytes) => Response::Ok(bytes),
            Err(e) => Response::Err(e.to_string()),
        };
        write_response(response);
    }
}

fn get_request() -> Rslt<Request> {
    let mut stdin = stdin();
    let mut len_buf = frame_length();
    stdin.read_exact(&mut len_buf)?;
    let len = from_len_frame(len_buf);
    let mut bytes = vec![0u8; len];
    stdin.read_exact(&mut bytes)?;
    return decode_from_slice::<Request, _>(&bytes, config())
        .map(|(r,_)| r).boxed()
}

fn run(request: Request) -> Rslt<Vec<u8>> {
    let result = match request {
        Request::TryRun => encode_to_vec(SimpleResult::Ok, config()),
        Request::GetUsage(arg) => encode_to_vec(get_usage(arg, None), config()),
        Request::GetMeta(arg) => encode_to_vec(get_meta(arg, None), config()),
        Request::GetMetas(arg) => encode_to_vec(get_metas(arg, None), config()),
        Request::GetTypedMeta(arg) => encode_to_vec(get_file_type(arg, None), config()),
        Request::GetTypedMetas(arg) => encode_to_vec(get_file_types(arg, None), config()),
        Request::CreateDir(arg) => encode_to_vec(create_dir(arg, None), config()),
        Request::CreateFile(arg) => encode_to_vec(create_file(arg, None), config()),
        Request::Delete(arg) => encode_to_vec(delete_by(arg, None), config()),
        Request::Copy(_from, _to, _moving) => panic!(),
    };
    return result.boxed();
}

fn write_response(response: Response) {
    let bytes = encode_to_vec(response, config())
        .expect("encode_to_vec failed");
    let mut stdout = stdout();
    let len_buf = to_len_frame(bytes.len());
    stdout.write_all(&len_buf)
        .expect("write_all len failed");
    stdout.write_all(&bytes)
        .expect("write_all bytes failed");
    stdout.flush()
        .expect("flush failed");
}
