
const DIMENS: &[char] = &['B', 'K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y', 'R', 'Q'];

pub trait HumanReadableSize {
    fn to_hr_size(&self) -> String;
}

impl HumanReadableSize for u64 {

    fn to_hr_size(&self) -> String {
        if *self == 0 {
            return "0B".to_string();
        }
        let mut dim = 0;
        let mut tmp = *self;
        let mut secondary = 0u64;
        while tmp >= 1024 && dim < DIMENS.len() - 1 {
            secondary = tmp % 1024;
            tmp /= 1024;
            dim += 1;
        }
        let mut out = String::new();
        if tmp <= 9 && secondary >= 950 {
            tmp += 1;
            secondary = 0;
        }
        out.push_str(&tmp.to_string());
        if out.len() == 1 && secondary >= 50 {
            let dec = ((secondary as f32) / 100.0).round() as i32;
            if dec > 0 {
                out.push('.');
                out.push_str(&dec.to_string());
            }
        }
        out.push(DIMENS[dim]);
        return out;
    }
}
