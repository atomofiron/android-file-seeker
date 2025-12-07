use std::error::Error;
use std::fmt::Display;

pub trait ResultExt<T, E> {
    #[allow(dead_code)]
    fn or_then<F: FnOnce(E) -> Result<T, E>>(self, op: F) -> Result<T, E>;
    #[allow(dead_code)]
    fn map_on<F: FnOnce(T) -> Option<R>, R>(self, op: F) -> Option<R>;
    fn boxed(self) -> Result<T, Box<dyn Error>> where E: Error + Send + Sync + 'static;
    fn if_err(self, f: impl FnOnce(E) -> ());
    fn err_to_string(self) -> Result<T, String> where E: Display;
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

    fn map_on<F: FnOnce(T) -> Option<R>, R>(self, op: F) -> Option<R> {
        // Result.Ok + Some = Some
        // Result.Err + Some = None
        match self {
            Ok(t) => op(t),
            Err(_) => None,
        }
    }

    fn boxed(self) -> Result<T, Box<dyn Error>> where E: Error + Send + Sync + 'static {
        self.map_err(Into::into)
    }

    fn if_err(self, f: impl FnOnce(E) -> ()) {
        match self {
            Ok(_) => (),
            Err(e) => f(e),
        }
    }

    fn err_to_string(self) -> Result<T, String> where E: Display {
        self.map_err(|e| e.to_string())
    }
}
