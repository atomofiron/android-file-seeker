use std::sync::mpsc::{channel, Receiver, Sender};
use crate::api::protocol::SimpleResult;

#[derive(uniffi::Object)]
pub struct WatchHandle {
    tx: Sender<()>,
}

impl WatchHandle {
    pub fn new() -> (Self, Receiver<()>) {
        let (tx, rx) = channel::<()>();
        return (Self { tx }, rx);
    }
}

#[uniffi::export]
impl WatchHandle {
    pub fn stop(&self) -> SimpleResult {
        match self.tx.send(()) {
            Ok(_) => SimpleResult::Ok,
            Err(e) => SimpleResult::Err(e.to_string()),
        }
    }
}
