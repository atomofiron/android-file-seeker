use crate::api::api::SimpleResult;
use crate::api::su_api::{control_frame, from_control_frame};
use crate::common::{config, Rslt, CHUNK_SIZE};
use crate::ext::raw_path::{RawPath, RawPathExt};
use crate::ext::result::ResultExt;
use crate::lib_main::write_response;
use crate::r#impl::other::read_error;
use crate::r#impl::reader::api::{FileReader, ReadResult, TextProvider};
use bincode::decode_from_slice;
use std::fs::File;
use std::io::{BufReader, Error, Read};
use std::ops::DerefMut;
use std::process::Child;
use std::sync::Mutex;

impl FileReader {

    pub fn new(inner: File) -> FileReader {
        let reader = BufReader::new(inner);
        let provider = TextProvider::Direct(reader);
        return Self { provider: Mutex::new(provider) }
    }

    pub fn with(child: Child, pid: u32) -> FileReader {
        let provider = TextProvider::Child(child, pid);
        return Self { provider: Mutex::new(provider) }
    }

    pub fn try_next(&self) -> Rslt<ReadResult> {
        let mut provider = self.provider.lock()
            .err_to_string()?;
        let result = match provider.deref_mut() {
            TextProvider::Direct(reader) => {
                let mut buf = vec![0u8; CHUNK_SIZE];
                reader.read(&mut buf)
                    .map(|count| {
                        buf.truncate(count);
                        match count {
                            0 => ReadResult::End,
                            _ => ReadResult::Ok(buf)
                        }
                    })?
            },
            TextProvider::Child(child, _) => try_next(child)?
        };
        return Ok(result);
    }
}

pub fn read_file(path: RawPath) -> SimpleResult {
    match try_read_file(path) {
        Ok(_) => SimpleResult::Ok,
        Err(e) => SimpleResult::Err(e.to_string()),
    }
}

fn try_read_file(path: RawPath) -> Rslt<()> {
    let file = File::open(path.buf())?;
    let reader = FileReader::new(file);
    loop {
        let result = reader.next();
        write_response(&result);
        if matches!(result, ReadResult::End) {
            break
        } else {
            // write_response(&result); ?
        }
    }
    return Ok(())
}

fn try_next(child: &mut Child) -> Rslt<ReadResult> {
    if let Ok(Some(status)) = child.try_wait() {
        match status.code() {
            Some(0) => return Ok(ReadResult::End),
            Some(code) => return Err(read_error(child, Error::other(format!("code: {code}"))))?,
            None => (),
        }
    }
    let stdout = child.stdout
        .as_mut()
        .ok_or("failed to open stdout for reading")?;
    let mut len_buf = control_frame();
    let read_result = stdout.read_exact(&mut len_buf);
    if let Err(e) = read_result {
        return Err(read_error(child, e))?;
    }
    let len = from_control_frame(len_buf) as usize;
    let mut bytes = vec![0u8; len];
    stdout.read_exact(&mut bytes)?;

    return decode_from_slice::<ReadResult,_>(&bytes, config())
        .map(|(result, _)| result)
        .boxed()
}
