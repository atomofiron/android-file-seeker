use crate::api::cancellation::CancellationState;
use crate::ext::raw_path::{RawPath, RawPathExt};
use ignore::{DirEntry, WalkBuilder, WalkState};
use std::sync::mpsc::Sender;
use std::sync::Arc;

pub fn walk<F, P: Send + Sync>(
    targets: Vec<RawPath>,
    sender: &Sender<P>,
    max_depth: usize,
    cancellation: Arc<dyn CancellationState>,
    action: F,
) where
    F: Fn(DirEntry, &Sender<P>) -> WalkState + Send + Sync,
{
    if targets.is_empty() {
        return;
    }
    let targets = targets.into_iter()
        .map(|it| it.buf())
        .collect::<Vec<_>>();
    let mut builder = WalkBuilder::new(&targets[0]);
    for path in targets.iter().skip(1) {
        builder.add(path);
    }
    let max_depth = match max_depth {
        0 => None,
        _ => Some(max_depth),
    };
    let walker = builder
        .standard_filters(false)
        .ignore(false)
        .git_ignore(false)
        .git_global(false)
        .git_exclude(false)
        .require_git(false)
        .hidden(false)
        .parents(false)
        .follow_links(false)
        .same_file_system(false)
        .max_depth(max_depth)
        .threads(num_cpus::get())
        .build_parallel();

    let action = Arc::new(action);
    let sender = Arc::new(sender);

    walker.run(|| {
        let action = Arc::clone(&action);
        let sender = Arc::clone(&sender);
        let cancellation = cancellation.clone();
        Box::new(move |result| {
            if cancellation.cancelled() {
                return WalkState::Quit;
            }
            match result {
                Ok(entry) => action(entry, &sender),
                Err(_) => WalkState::Continue, // .gitignore errors (unreachable)
            }
        })
    });
}
