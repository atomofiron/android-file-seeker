use crate::api::protocol::TextSearchProgress;
use crate::ext::raw_path::PathExt;
use crate::r#impl::meta::meta_with_error;
use grep_matcher::Matcher;
use grep_searcher::{Searcher, Sink, SinkMatch};
use std::path::Path;
use std::sync::mpsc::Sender;

pub struct TextSink<'l, M: Matcher> {
    pub matcher: &'l M,
    pub path: &'l Path,
    pub sender: &'l Sender<TextSearchProgress>,
}

impl <'l, M: Matcher>Sink for TextSink<'l, M> {
    type Error = Box<dyn std::error::Error>;

    fn matched(&mut self, _searcher: &Searcher, mat: &SinkMatch) -> Result<bool, Self::Error> {
        let line = mat.bytes();
        let result = self.matcher.find_iter(line, |m| {
            let progress = TextSearchProgress::Ok {
                path: self.path.to_path_buf().raw(),
                offset: mat.absolute_byte_offset() + m.start() as u64,
                length: (m.end() - m.start()) as u32,
                line: mat.line_number().map(|n| n - 1),
            };
            let _ = self.sender.send(progress);
            true
        });
        if let Err(e) = result {
            let err = TextSearchProgress::Err(meta_with_error(&self.path.to_path_buf(), &e));
            self.sender.send(err)?;
        }
        Ok(true)
    }
}
