use bincode::{decode_from_slice, encode_to_vec};
use native_lib::api::bridge::{create_dir, create_file, delete_by, get_file_type, get_file_types, get_meta, get_metas, get_usage};
use native_lib::api::request::Request;
use native_lib::common::config;
use std::io::{stdin, stdout, Read, Write};
use native_lib::api::protocol::SimpleResult;

fn main() {
    let mut bytes = Vec::new();
    let mut stdin = stdin();
    stdin.read_to_end(&mut bytes).expect("read_to_end failed");
    drop(stdin);
    let (request, _) = decode_from_slice::<Request,_>(&bytes, config()).expect("decode failed");
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
    };
    let bytes = result.expect("encode failed");
    let mut stdout = stdout();
    stdout.write_all(&bytes).expect("write_all failed");
    drop(stdout);
}
