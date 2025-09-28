
pub trait OptionExt<T, E> {
    fn or_then<F: FnOnce() -> Result<T, E>>(self, op: F) -> Result<T, E>;
}

impl<T, E> OptionExt<T, E> for Option<T> {
    fn or_then<F: FnOnce() -> Result<T, E>>(self, op: F) -> Result<T, E> {
        // Option0.Some + Result1 = Result0
        // Option0.None + Result1 = Result1
        match self {
            Some(t) => Ok(t),
            None => op(),
        }
    }
}
