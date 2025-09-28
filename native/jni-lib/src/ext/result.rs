use std::error::Error;

pub trait ResultExt<T, E> {
    #[allow(dead_code)]
    fn or_then<F: FnOnce(E) -> Result<T, E>>(self, op: F) -> Result<T, E>;
    fn boxed(self) -> Result<T, Box<dyn Error>> where E: Error + Send + Sync + 'static;
}

impl<T, E> ResultExt<T, E> for Result<T, E> {

    fn or_then<F: FnOnce(E) -> Result<T, E>>(self, op: F) -> Result<T, E> {
        // Result0.Ok + Result1 = Result0
        // Result0.Err + Result1 = Result1
        match self {
            Ok(t) => Ok(t),
            Err(e) => op(e),
        }
    }

    fn boxed(self) -> Result<T, Box<dyn Error>> where E: Error + Send + Sync + 'static {
        self.map_err(Into::into)
    }
}
