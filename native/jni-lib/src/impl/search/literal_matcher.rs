use bstr::ByteSlice;
use grep_matcher::{Match, Matcher, NoCaptures};

pub struct LiteralMatcher {
    pub query: Vec<u8>,
    pub case_insensitive: bool,
}

impl LiteralMatcher {

    pub fn new(query: String, case_insensitive: bool) -> LiteralMatcher {
        LiteralMatcher {
            query: match case_insensitive {
                true => query.to_lowercase(),
                false => query,
            }.into_bytes(),
            case_insensitive,
        }
    }

    fn position(&self, text: &[u8]) -> Option<usize> {
        if text.len() < self.query.len() {
            return None;
        }
        match self.case_insensitive {
            true => self.find(text.to_lowercase().as_slice()),
            false => self.find(text),
        }
    }

    fn find(&self, text: &[u8]) -> Option<usize> {
        if self.query.is_empty() {
            return None;
        }
        text.windows(self.query.len())
            .position(|w| w == self.query)
    }
}

impl Matcher for LiteralMatcher {
    type Captures = NoCaptures;
    type Error = String;

    fn find_at(&self, haystack: &[u8], at: usize) -> Result<Option<Match>, Self::Error> {
        if at >= haystack.len() {
            return Ok(None);
        }
        if let Some(position) = self.position(&haystack[at..]) {
            let start = at + position;
            let end = start + self.query.len();
            Ok(Some(Match::new(start, end)))
        } else {
            Ok(None)
        }
    }

    fn new_captures(&self) -> Result<Self::Captures, Self::Error> {
        Ok(NoCaptures::new())
    }
}
