use crate::api::protocol::{SearchQuery, TextMatch};
use crate::common::{Rslt, EMPTY_VALUE_ERROR};
use crate::r#impl::search::literal_matcher::LiteralMatcher;
use crate::r#impl::search::text_matches::TextMatches;
use crate::r#impl::search::text_sink::TextSink;
use grep_regex::{RegexMatcher, RegexMatcherBuilder};
use grep_searcher::SearcherBuilder;
use std::path::Path;

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

    pub fn search(&self, path: &Path) -> Rslt<Vec<TextMatch>> {
        let mut searcher = SearcherBuilder::new()
            .line_number(true)
            .build();
        let matches = TextMatches::new();
        match &self {
            TextMatcher::Literal(matcher) => searcher.search_path(matcher, path, TextSink::new(&matcher, &matches))?,
            TextMatcher::Regex(matcher) => searcher.search_path(matcher, path, TextSink::new(&matcher, &matches))?,
        };
        return Ok(matches.take());
    }
}
