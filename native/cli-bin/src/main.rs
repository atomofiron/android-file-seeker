use bincode::{decode_from_slice, encode_to_vec};
use native_lib::api::bridge::{create_dir, create_file, delete_by, get_file_type, get_file_types, get_meta, get_metas, get_usage};
use native_lib::api::su_protocol::{frame_length, from_len_frame, to_len_frame, Request};
use native_lib::common::config;
use std::io::{stdin, stdout, Read, Write};
use native_lib::api::protocol::SimpleResult;

fn main() {
    let mut len_buf = frame_length();
    loop {
        let mut stdin = stdin();
        stdin.read_exact(&mut len_buf)
            .expect("read len failed");
        let len = from_len_frame(len_buf);
        let mut bytes = vec![0u8; len];
        stdin.read_exact(&mut bytes)
            .expect("read bytes failed");
        let (request, _) = decode_from_slice::<Request, _>(&bytes, config())
            .expect(format!("decode failed, bytes: {}", bytes.len()).as_str());

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
        let len_buf = to_len_frame(bytes.len());

        stdout.write_all(&len_buf)
            .expect("write_all len failed");
        stdout.write_all(&bytes)
            .expect("write_all bytes failed");
        stdout.flush()
            .expect("flush failed");
    }
}
