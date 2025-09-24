use native_lib::features::r#type::file_type;

fn main() {
    let _ = file_type("/sdcard/".to_string());
}