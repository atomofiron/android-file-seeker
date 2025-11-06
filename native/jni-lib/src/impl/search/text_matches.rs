use crate::api::protocol::TextMatch;
use std::cell::RefCell;

pub struct TextMatches {
    matches: RefCell<Vec<TextMatch>>,
}

impl TextMatches {

    pub fn new() -> Self {
        TextMatches {
            matches: RefCell::new(vec![])
        }
    }

    pub fn push(&self, matcher: TextMatch) {
        self.matches.borrow_mut().push(matcher);
    }

    pub fn take(self) -> Vec<TextMatch> {
        self.matches.into_inner()
    }
}
