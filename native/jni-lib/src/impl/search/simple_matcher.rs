use crate::api::api::SearchQuery;
use crate::r#impl::search::matcher::NameMatcher;

pub struct SimpleMatcher {
    query: String,
    case_insensitive: bool,
}

impl SimpleMatcher {

    pub fn new(query: &SearchQuery) -> Self {
        Self {
            query: match query.case_insensitive {
                true => query.query.clone(),
                false => query.query.to_lowercase(),
            },
            case_insensitive: query.case_insensitive,
        }
    }
}

impl NameMatcher for SimpleMatcher {

    fn matches(&self, filename: &std::ffi::OsStr) -> bool {
        let filename_str = filename.to_string_lossy();
        match self.case_insensitive {
            true => filename_str.to_lowercase().contains(&self.query),
            false => filename_str.contains(&self.query),
        }
    }
}
