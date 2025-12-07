use crate::api::api::{FileEvent, FileEventCollector};
use crate::common::Rslt;
use crate::ext::option::OptionExt;
use crate::ext::raw_path::{PathExt, RawPath, RawPathExt};
use crate::ext::result::ResultExt;
use crate::r#impl::inotify::api::WatchHandle;
use inotify::{EventMask, Inotify, WatchDescriptor, WatchMask, Watches};
use std::collections::HashMap;
use std::path::PathBuf;
use std::sync::mpsc::{channel, Sender};
use std::sync::Arc;
use std::thread;

enum Msg {
    Descriptor(Watches, WatchDescriptor),
    Event(FileEvent),
    Stop,
}

pub fn try_observe_dir(path: RawPath, collector: Arc<dyn FileEventCollector>) -> Rslt<Arc<WatchHandle>> {
    let (handle, rx_cancel) = WatchHandle::new();
    let handle = Arc::new(handle);
    let inotify = Inotify::init()?;

    let (tx, rx_msg) = channel::<Msg>();
    let tx_fs = tx.clone();
    let tx_cancel = tx.clone();

    let collector_ref = collector.clone();
    let _ = thread::spawn(move || {
        observe_dir(path.buf(), inotify, tx_fs)
            .unwrap_or_else(|e| collector_ref.error(e.to_string()));
    });
    let collector_ref = collector.clone();
    let _ = thread::spawn(move || {
        rx_cancel.recv().boxed()
            .and_then(|_| tx_cancel.send(Msg::Stop).boxed())
            .unwrap_or_else(|e| collector_ref.error(e.to_string()));
    });
    let _ = thread::spawn(move || {
        let mut descriptor: Option<(Watches,WatchDescriptor)> = None;
        loop {
            match rx_msg.recv() {
                Ok(Msg::Event(event)) => collector.emit(event),
                Ok(Msg::Stop) => break,
                Ok(Msg::Descriptor(w, wd)) => descriptor = Some((w, wd)),
                Err(e) => break collector.error(e.to_string()),
            }
        }
        descriptor.and_try(|(mut w,d)| w.remove(d))
            .if_none(|| collector.error("no descriptor".to_string()));
    });
    return Ok(handle);
}

fn observe_dir(
    path: PathBuf,
    mut inotify: Inotify,
    tx: Sender<Msg>,
) -> Rslt<()> {
    let mut watches = inotify.watches();
    let descriptor = watches.add(&path, WatchMask::CREATE | WatchMask::DELETE | WatchMask::MOVED_FROM | WatchMask::MOVED_TO)?;
    tx.send(Msg::Descriptor(watches, descriptor))?;
    let mut buffer = [0u8; 4096];
    let mut moves: HashMap<u32, RawPath> = HashMap::new();
    loop {
        let events = inotify.read_events_blocking(&mut buffer)?;
        for event in events {
            let path = match event.name {
                Some(os) => os.raw(),
                None => continue,
            };
            let event: FileEvent = if event.mask.contains(EventMask::CREATE) {
                match path.hidden() {
                    true => continue,
                    false => FileEvent::Create(path),
                }
            } else if event.mask.contains(EventMask::DELETE) {
                match path.hidden() {
                    true => continue,
                    false => FileEvent::Delete(path),
                }
            } else if event.mask.contains(EventMask::MOVED_FROM) {
                moves.insert(event.cookie, path);
                continue;
            } else if event.mask.contains(EventMask::MOVED_TO) {
                match moves.remove(&event.cookie) {
                    Some(from) if from.hidden() && path.hidden() => continue,
                    Some(from) if path.hidden() => FileEvent::Delete(from),
                    Some(from) if from.hidden() => FileEvent::Create(path),
                    Some(from) => FileEvent::Move { from, to: path },
                    None => continue,
                }
            } else {
                continue;
            };
            tx.send(Msg::Event(event))?;
        }
    }
}
