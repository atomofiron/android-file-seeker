use std::path::Path;
use std::sync::mpsc::Sender;
use crate::api::protocol::{SearchQuery, TextSearchProgress};
use crate::common::{Rslt, EMPTY_VALUE_ERROR};
use grep_regex::{RegexMatcher, RegexMatcherBuilder};
use grep_searcher::Searcher;
use crate::r#impl::search::literal_matcher::LiteralMatcher;
use crate::r#impl::search::text_sink::TextSink;

pub enum TextMatcher {
    Literal(LiteralMatcher),
    Regex(RegexMatcher),
}

impl TextMatcher {

    pub fn new(query: SearchQuery) -> Rslt<Self> {
        if query.query.is_empty() {
            return Err(EMPTY_VALUE_ERROR.into());
        }
        let matcher = match query.regex {
            false => Self::Literal(LiteralMatcher::new(query.query, query.case_insensitive)),
            true => {
                let matcher = RegexMatcherBuilder::new()
                    .case_insensitive(query.case_insensitive)
                    .build(query.query.as_str())?;
                Self::Regex(matcher)
            },
        };
        return Ok(matcher);
    }

    pub fn search(&self, path: &Path, sender: &Sender<TextSearchProgress>) -> Rslt<()> {
        let mut searcher = Searcher::new();
        return match &self {
            TextMatcher::Literal(matcher) => {
                let sink = TextSink { matcher, path, sender };
                searcher.search_path(&matcher, path, sink)
            },
            TextMatcher::Regex(matcher) => {
                let sink = TextSink { matcher, path, sender };
                searcher.search_path(&matcher, path, sink)
            },
        };
    }
}
