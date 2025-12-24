use crate::api::api::TypedMeta;
use crate::common::{empty_string, Rslt, AUDIO_OGG, VIDEO_OGG};
use crate::r#impl::hr_meta::HumanReadableMeta;
use crate::r#impl::meta::meta_with_error;
use ogg::PacketReader;
use std::fs;
use std::fs::File;
use std::path::PathBuf;

pub fn file_type(path: &PathBuf) -> Rslt<TypedMeta> {
    let mime = tree_magic_mini::from_filepath(path);
    let mut metadata = path.metadata();
    if mime == None {
        metadata = Ok(metadata?); // check both of them
    };
    let entry = TypedMeta {
        meta: metadata.to_hr(path),
        mime: mime.map(|m| check(&path, m)).unwrap_or(empty_string()),
    };
    return Ok(entry)
}

pub fn file_types(path: &PathBuf) -> Rslt<Vec<TypedMeta>> {
    let dir = fs::read_dir(path)?;
    let entries = dir.filter_map(|entry| {
        entry.ok().map(|e| e.path()).and_then(|path| {
            let meta = path.metadata().to_hr(&path);
            tree_magic_mini::from_filepath(&path)
                .map(|mime| TypedMeta { meta, mime: check(&path, mime) })
        })
    }).collect::<Vec<_>>();
    return Ok(entries);
}

pub fn type_or_meta(path: &PathBuf) -> TypedMeta {
    file_type(path).unwrap_or_else(|e| {
        TypedMeta {
            meta: meta_with_error(&path.into(), &e),
            mime: empty_string(),
        }
    })
}

fn check(path: &PathBuf, mime: &str) -> String {
    return match mime {
        VIDEO_OGG => match check_ogg_audio(path) {
            Ok(true) => AUDIO_OGG.to_string(),
            _ => mime.to_string()
        }
        _ => mime.to_string()
    };
}

fn check_ogg_audio(path: &PathBuf) -> Rslt<bool> {
    let file = File::open(path)?;
    let mut reader = PacketReader::new(file);
    let bytes = reader.read_packet()?
        .map(|p| p.data)
        .unwrap_or(vec![]);
    let audio = bytes.starts_with(&[0x01, b'v', b'o', b'r', b'b', b'i', b's']) ||
        bytes.starts_with(b"OpusHead") ||
        bytes.starts_with(b"fLaC") ||
        bytes.starts_with(b"Speex") ||
        bytes.starts_with(b"CELT") ||
        bytes.starts_with(b"PCM");
    return Ok(audio);
}
