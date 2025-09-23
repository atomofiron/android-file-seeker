use crate::bridge;
use crate::features::meta::{meta, metas};
use crate::features::other::{delete, mkdir, mkfile, usage};
use crate::features::r#type::{file_type, file_types};
use crate::staff::Rslt;
use bridge::result_msg::Data;
use bridge::{Command, CommandMsg, Metas, ResultMsg};
use jni::errors::Result as JniResult;
use jni::objects::{GlobalRef, JByteArray, JClass, JObject, JObjectArray, JString, JValue};
use jni::JNIEnv;
use prost::Message;
use std::thread;

#[no_mangle]
pub extern "system" fn Java_app_atomofiron_searchboxapp_android_NativeBridge_run<'local>(
    mut env: JNIEnv<'local>,
    _: JClass,
    command: JByteArray,
    args: JObjectArray,
) -> JByteArray<'local> {
    let cmd = get_cmd(&env, command);
    let argv = jstring_array_to_vec(&mut env, args).unwrap_or_default();
    let result = cmd.and_then(|cmd| run(cmd, argv))
        .unwrap_or_else(|e| ResultMsg {
            data: Some(Data::Error(e.to_string())),
        });
    return to_bytes(&env, result);
}

#[no_mangle]
pub extern "system" fn Java_app_atomofiron_searchboxapp_android_NativeBridge_runAsync<'local>(
    mut env: JNIEnv<'local>,
    _: JClass,
    command: JByteArray,
    args: JObjectArray,
    callback: JObject,
) {
    let _cmd = get_cmd(&env, command);
    let _argv = jstring_array_to_vec(&mut env, args).unwrap_or_default();

    let jvm = env.get_java_vm().expect("get_java_vm");
    let cb_global: GlobalRef = env
        .new_global_ref(callback)
        .expect("new_global_ref");

    thread::spawn(move || {
        let data = Data::Error("err".to_string());
        let msg = ResultMsg {
            data: Some(data),
        };
        let mut env = jvm.attach_current_thread().expect("attach_current_thread");
        let out = to_bytes(&env, msg);
        let _ = env.call_method(
            &cb_global,
            "invoke", // important: Kotlin Function1 becomes (Ljava/lang/Object;)Ljava/lang/Object;
            "(Ljava/lang/Object;)Ljava/lang/Object;",
            &[JValue::Object(&JObject::from(out))],
        );
    });
}

fn parse_cmd(bytes: &[u8]) -> Rslt<Command> {
    let msg = CommandMsg::decode(bytes)?;
    let msg = Command::try_from(msg.cmd)?;
    return Ok(msg);
}

fn jstring_array_to_vec(env: &mut JNIEnv, arr: JObjectArray) -> JniResult<Vec<String>> {
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

fn get_cmd(env: &JNIEnv, command: JByteArray) -> Rslt<Command> {
    let cmd_bytes: Vec<u8> = env
        .convert_byte_array(command)
        .unwrap_or_default();
    return parse_cmd(&*cmd_bytes);
}

fn to_bytes<'l>(env: &JNIEnv<'l>, response: ResultMsg) -> JByteArray<'l> {
    env.byte_array_from_slice(&response.encode_to_vec()).unwrap()
}

fn run(command: Command, argv: Vec<String>) -> Rslt<ResultMsg> {
    let first_arg = argv.first();
    return match command {
        Command::Meta => meta(first_arg.unwrap()).map(|it| ResultMsg { data: Some(Data::Meta(it)) }),
        Command::Metas => metas(first_arg.unwrap()).map(|entries| ResultMsg { data: Some(Data::Metas(Metas { entries })) }),
        Command::Type => Ok(ResultMsg { data: Some(Data::Type(file_type(first_arg.unwrap()))) }),
        Command::Types => file_types(first_arg.unwrap()).map(|entries| ResultMsg { data: Some(Data::Types(entries)) }),
        Command::Mkfile => mkfile(first_arg.unwrap()).map(|it| ResultMsg { data: Some(Data::Meta(it)) }),
        Command::Mkdir => mkdir(first_arg.unwrap()).map(|it| ResultMsg { data: Some(Data::Meta(it)) }),
        Command::Usage => usage(first_arg.unwrap()).map(|it| ResultMsg { data: Some(Data::Usage(it)) }),
        Command::Delete => Ok(ResultMsg { data: Some(Data::Ok(delete(first_arg.unwrap()))) }),
    };
}
