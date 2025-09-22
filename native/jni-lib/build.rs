fn main() {
    prost_build::compile_protos(&["../../proto/bridge.proto"], &["../../proto/"]).unwrap();
}