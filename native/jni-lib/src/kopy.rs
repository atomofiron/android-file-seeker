#[macro_export]
macro_rules! kopy {
    ($orig:expr) => {
        $orig.clone()
    };
    ($orig:expr, $($field:ident = $value:expr),+ $(,)?) => {
        {
            let mut tmp = $orig.clone();
            $( tmp.$field = $value; )+
            tmp
        }
    };
}