use crate::api::api::SearchQuery;
use crate::common::Rslt;
use crate::r#impl::search::regex_matcher::RegexMatcher;
use crate::r#impl::search::simple_matcher::SimpleMatcher;

pub trait NameMatcher: Send + Sync {
    fn matches(&self, text: &std::ffi::OsStr) -> bool;
}

pub fn build_matcher(query: &SearchQuery) -> Rslt<Box<dyn NameMatcher>> {
    let matcher: Box<dyn NameMatcher> = match query.regex {
        true => Box::new(RegexMatcher::new(query)?),
        false => Box::new(SimpleMatcher::new(query)),
    };
    return Ok(matcher);
}
