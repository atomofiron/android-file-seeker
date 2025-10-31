use bincode::{encode_to_vec, Encode};
use bincode::error::EncodeError;
use crate::common::config;

pub trait EncodeExt: Encode {
    fn to_bytes(&self) -> Result<Vec<u8>, EncodeError> {
        encode_to_vec(self, config())
    }
}

impl<T: Encode> EncodeExt for T {}
