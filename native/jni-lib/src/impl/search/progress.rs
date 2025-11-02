use crate::api::su_protocol::ProgressProxy;
use crate::common::Rslt;
use std::sync::mpsc::Receiver;
use std::thread::JoinHandle;

pub fn proxy_progress<D : Send + Sync + 'static>(
    rx: Receiver<D>,
    collector: Box<dyn ProgressProxy<D>>,
) -> Rslt<JoinHandle<()>> {
    let handle = std::thread::spawn(move || {
        loop {
            let keep_doing = match rx.recv() {
                Ok(progress) => collector.emit(progress),
                Err(_) => break,
            };
            if !keep_doing {
                break;
            }
        }
    });
    return Ok(handle);
}
