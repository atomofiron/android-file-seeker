use regex::{Regex, RegexBuilder};
use crate::api::api::SearchQuery;
use crate::common::Rslt;
use crate::r#impl::search::matcher::NameMatcher;

pub struct RegexMatcher {
    regex: Regex,
}

impl RegexMatcher {

    pub fn new(query: &SearchQuery) -> Rslt<Self> {
        let regex = RegexBuilder::new(&query.query)
            .case_insensitive(query.case_insensitive)
            .build()?;
        Ok(Self { regex })
    }
}

impl NameMatcher for RegexMatcher {

    fn matches(&self, filename: &std::ffi::OsStr) -> bool {
        let filename_str = filename.to_string_lossy();
        self.regex.is_match(&filename_str)
    }
}
