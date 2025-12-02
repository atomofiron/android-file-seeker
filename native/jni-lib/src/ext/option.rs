use std::fmt::Display;

pub trait OptionExt<T> {
    fn or_then<E, F: FnOnce() -> Result<T, E>>(self, op: F) -> Result<T, E>;
    fn and_try<R, E, F: FnOnce(T) -> Result<R, E>>(self, op: F) -> Option<R>;
    fn if_none<F: FnOnce()>(self, f: F);
    fn or_err<E : Display, F: FnOnce() -> E>(self, f: F) -> Result<T, E>;
}

impl<T> OptionExt<T> for Option<T> {

    fn or_then<E, F: FnOnce() -> Result<T, E>>(self, op: F) -> Result<T, E> {
        // Option0.Some + Result1 = Result0
        // Option0.None + Result1 = Result1
        match self {
            Some(t) => Ok(t),
            None => op(),
        }
    }

    fn and_try<R, E, F: FnOnce(T) -> Result<R, E>>(self, op: F) -> Option<R> {
        // Option0.Some + Result1 = Some1
        // Option0.None + Result1 = None
        match self {
            Some(t) => match op(t) {
                Ok(r) => Some(r),
                Err(_) => None,
            },
            None => None,
        }
    }

    fn if_none<F: FnOnce()>(self, f: F) {
        match self {
            Some(_) => (),
            None => f(),
        }
    }

    fn or_err<E : Display, F: FnOnce() -> E>(self, f: F) -> Result<T, E> {
        match self {
            Some(t) => Ok (t),
            None => Err(f()),
        }
    }
}
