use jni::objects::{GlobalRef, JByteArray, JClass, JObject, JObjectArray, JString, JValue};
use jni::JNIEnv;
use std::thread;

#[no_mangle]
pub extern "system" fn Java_app_atomofiron_searchboxapp_android_NativeBridge_run<'local>(
    mut env: JNIEnv<'local>,
    _clazz: JClass,
    command: JString,
    args: JObjectArray,
) -> JByteArray<'local> {
    let cmd: String = match env.get_string(&command) {
        Ok(s) => s.into(),
        Err(_) => String::new(),
    };
    let argv = jstring_array_to_vec(&mut env, args).unwrap_or_default();
    let result: Vec<u8> = do_execute(cmd, argv);
    return env.byte_array_from_slice(&result).unwrap();
}

#[no_mangle]
pub extern "system" fn Java_app_atomofiron_searchboxapp_android_NativeBridge_runAsync<'local>(
    mut env: JNIEnv<'local>,
    _clazz: JClass,
    command: JString,
    args: JObjectArray,
    callback: JObject,
) {
    let cmd: String = env.get_string(&command).map(|s| s.into()).unwrap_or_default();
    let argv = jstring_array_to_vec(&mut env, args).unwrap_or_default();

    let jvm = env.get_java_vm().expect("get_java_vm");
    let cb_global: GlobalRef = env
        .new_global_ref(callback)
        .expect("new_global_ref");

    thread::spawn(move || {
        let bytes: Vec<u8> = do_execute(cmd, argv);
        let mut env = jvm.attach_current_thread().expect("attach_current_thread");
        let out = env
            .byte_array_from_slice(&bytes)
            .expect("byte_array_from_slice");
        let _ = env.call_method(
            &cb_global,
            "invoke", // important: Kotlin Function1 becomes (Ljava/lang/Object;)Ljava/lang/Object;
            "(Ljava/lang/Object;)Ljava/lang/Object;",
            &[JValue::Object(&JObject::from(out))],
        );
    });
}

fn jstring_array_to_vec(env: &mut JNIEnv, arr: JObjectArray) -> jni::errors::Result<Vec<String>> {
    let len = env.get_array_length(&arr)?;
    let mut out = Vec::with_capacity(len as usize);
    for i in 0..len {
        let elem = env.get_object_array_element(&arr, i)?;
        let jstr: JString = elem.into();
        let string: String = env.get_string(&jstr)?.into();
        out.push(string);
    }
    Ok(out)
}

fn do_execute(cmd: String, args: Vec<String>) -> Vec<u8> {
    let s = format!("cmd={cmd}, args={args:?}");
    return s.into_bytes();
}
