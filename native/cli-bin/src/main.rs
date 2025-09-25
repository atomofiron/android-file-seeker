use std::io::{stdout, Write};
use bincode::encode_to_vec;
use native_lib::api::protocol::SimpleResult;
use native_lib::common::config;

fn main() {
    let result = SimpleResult::Error("Test error".to_string());
    let bytes = encode_to_vec(result, config()).expect("encode failed");
    let mut stdout = stdout();
    stdout.write_all(&bytes).expect("write_all failed");
    drop(stdout);
}