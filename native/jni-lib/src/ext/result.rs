
// Result0.Ok + Result1 = Result0
// Result0.Err + Result1 = Result1
pub trait ResultExt<T, E> {
    #[allow(dead_code)]
    fn or_then<F: FnOnce(E) -> Result<T, E>>(self, op: F) -> Result<T, E>;
}

impl<T, E> ResultExt<T, E> for Result<T, E> {
    fn or_then<F: FnOnce(E) -> Result<T, E>>(self, op: F) -> Result<T, E> {
        match self {
            Ok(t) => Ok(t),
            Err(e) => op(e),
        }
    }
}
