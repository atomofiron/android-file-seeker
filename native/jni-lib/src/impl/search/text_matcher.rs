use crate::api::api::{SearchQuery, TextMatch};
use crate::common::{Rslt, EMPTY_VALUE_ERROR};
use crate::r#impl::search::text_matches::TextMatches;
use crate::r#impl::search::text_sink::TextSink;
use grep_regex::{RegexMatcher, RegexMatcherBuilder};
use grep_searcher::{Encoding, SearcherBuilder};
use std::path::Path;

pub trait TextMatcher {
    fn from_query(query: SearchQuery) -> Rslt<RegexMatcher>;
    fn search(&self, path: &Path, encoding: Encoding, bom_offset: u64) -> Rslt<Vec<TextMatch>>;
}

impl TextMatcher for RegexMatcher {

    fn from_query(query: SearchQuery) -> Rslt<RegexMatcher> {
        if query.query.is_empty() {
            return Err(EMPTY_VALUE_ERROR.into());
        }
        let matcher = RegexMatcherBuilder::new()
            .case_insensitive(query.case_insensitive)
            .fixed_strings(!query.regex)
            .build(query.query.as_str())?;
        return Ok(matcher);
    }

    fn search(&self, path: &Path, encoding: Encoding, bom_offset: u64) -> Rslt<Vec<TextMatch>> {
        let mut searcher = SearcherBuilder::new()
            .line_number(true)
            .encoding(Some(encoding))
            .build();
        let matches = TextMatches::new();
        searcher.search_path(self, path, TextSink::new(&self, &matches, bom_offset))?;
        return Ok(matches.take());
    }
}
