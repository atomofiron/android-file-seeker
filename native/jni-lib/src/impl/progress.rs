use crate::api::protocol::Progress;
use crate::ext::raw_path::RawPath;

pub enum ProgressChange {
    Update(f32),
    Increment(f32),
    Err(RawPath, f32),
}

impl Progress {

    pub fn new() -> Self {
        Progress { count: 0, errors: 0, progress: 0.0 }
    }

    pub fn inc(&mut self, progress: f32) {
        self.count += 1;
        self.progress = progress;
    }

    pub fn inc_error(&mut self, progress: f32) {
        self.errors += 1;
        self.progress = progress;
    }

    pub fn update(&mut self, progress: f32) {
        self.progress = progress;
    }
}
