use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::Arc;

pub type Cancellation = Arc<AtomicBool>;

pub struct CancellationHandle {
    cancelled: Arc<AtomicBool>,
}

impl CancellationHandle {

    pub fn new() -> Self {
        Self {
            cancelled: Arc::new(AtomicBool::new(false)),
        }
    }

    pub fn cancel(&self) {
        self.cancelled.store(true, Ordering::Relaxed);
    }

    pub fn is_cancelled(&self) -> bool {
        self.cancelled.load(Ordering::Relaxed)
    }
}

#[uniffi::export(with_foreign)]
pub trait CancellationState : Send + Sync {
    fn cancelled(&self) -> bool;
}

impl CancellationState for CancellationHandle {

    fn cancelled(&self) -> bool {
        self.is_cancelled()
    }
}

impl CancellationState for () {
    fn cancelled(&self) -> bool {
        false
    }
}
