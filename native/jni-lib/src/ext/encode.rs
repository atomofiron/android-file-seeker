use bincode::{encode_to_vec, Encode};
use bincode::error::EncodeError;
use crate::common::config;

pub type EncodedResult = Result<Vec<u8>, EncodeError>;

pub trait EncodeExt: Encode {
    fn to_bytes(&self) -> EncodedResult;
}

impl<T: Encode> EncodeExt for T {
    fn to_bytes(&self) -> EncodedResult {
        encode_to_vec(self, config())
    }
}
