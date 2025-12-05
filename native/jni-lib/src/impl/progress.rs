use crate::api::protocol::{CommonProgress, CommonProgressCollector, CountingResult};
use crate::common::Rslt;
use crate::ext::raw_path::RawPath;
use crate::ext::result::ResultExt;
use crate::r#impl::hr_meta::HumanReadableMeta;
use std::ops::Range;
use std::path::PathBuf;
use std::sync::mpsc::{Receiver, Sender};
use std::sync::Arc;
use std::thread::JoinHandle;

pub enum ProgressChange {
    Update(f32),
    Increment(f32),
    Err(RawPath, String, f32),
}

impl CommonProgress {

    pub fn new() -> Self {
        CommonProgress { count: 0, errors: 0, progress: 0.0 }
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

pub fn convert_progress(
    rx: Receiver<ProgressChange>,
    collector: Arc<dyn CommonProgressCollector>,
    target: PathBuf,
) -> JoinHandle<CountingResult> {
    std::thread::spawn(move || {
        let mut progress = CommonProgress::new();
        collector.emit(progress.clone());
        let mut errors: Vec<(RawPath,String)> = Vec::new();
        loop {
            match rx.recv() {
                Ok(ProgressChange::Increment(new)) => progress.inc(new),
                Ok(ProgressChange::Update(new)) => progress.update(new),
                Ok(ProgressChange::Err(path, message, new)) => {
                    progress.inc_error(new);
                    errors.push((path, message))
                },
                Err(_) => break,
            }
            collector.emit(progress.clone());
        }
        let errors = errors.iter()
            .map(|(path, msg)| format!("{}: {msg}", String::from_utf8_lossy(path)))
            .collect();
        let meta = target.metadata()
            .map(|m| Ok(m).to_hr(&target))
            .ok();
        return CountingResult::Ok { count: progress.count, errors, meta }
    })
}

pub fn send_inc(
    tx: &Sender<ProgressChange>,
    range: &Range<f32>,
) -> Rslt<()> {
    let change = ProgressChange::Increment(range.end);
    return tx.send(change).boxed();
}
