use crate::api::api::TextMatch;
use crate::common::{string, Rslt};
use crate::ext::option::OptionExt;
use crate::r#impl::search::text_matches::TextMatches;
use grep_matcher::Matcher;
use grep_searcher::{Searcher, Sink, SinkMatch};

pub struct TextSink<'l, M: Matcher> {
    matcher: &'l M,
    matches: &'l TextMatches,
    bom_offset: u64,
}

impl<'l, M: Matcher> TextSink<'l, M> {
    pub fn new(
        matcher: &'l M,
        matches: &'l TextMatches,
        bom_offset: u64,
    ) -> Self {
        TextSink { matcher, matches, bom_offset }
    }
}

impl<'l, M: Matcher> Sink for TextSink<'l, M> {
    type Error = Box<dyn std::error::Error>;

    fn matched(&mut self, _searcher: &Searcher, mat: &SinkMatch) -> Result<bool, Self::Error> {
        let line = mat.bytes();
        let _ = self.matcher.try_find_iter(line, |m| {
            let the_match = TextMatch {
                offset: self.bom_offset + mat.absolute_byte_offset() + m.start() as u64,
                length: (m.end() - m.start()) as u32,
                line: mat.line_number()
                    .or_err(|| string("SearcherBuilder::line_number = false"))? - 1,
            };
            self.matches.push(the_match);
            Rslt::<_>::Ok(true)
        }); // no way for Error, because of matched = |m| Ok(matched(m))
        Ok(true)
    }
}
