use libloading::{Library, Symbol};
use std::env;

const LIB_NAME: &str = "libnative_lib.so";

fn main() {
    unsafe {
        let exe = env::current_exe()
            .expect("current executable path invalid");
        let dir = exe.parent()
            .expect("executable parent invalid");

        let lib = Library::new(dir.join(LIB_NAME))
            .expect(format!("failed to load {LIB_NAME} in {}", dir.to_string_lossy()).as_str());

        let lib_main: Symbol<unsafe extern "C" fn()> = lib.get(b"lib_main\0")
            .expect("failed to find lib_main");

        lib_main();
    }
}
