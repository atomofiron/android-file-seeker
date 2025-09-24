use std::fs::Metadata;
use std::os::unix::fs::MetadataExt;
use users::{get_user_by_uid, get_group_by_gid};

pub trait HumanReadableUsers {
    fn to_hr_owner(&self) -> String;
    fn to_hr_group(&self) -> String;
}

impl HumanReadableUsers for Metadata {

    fn to_hr_owner(&self) -> String {
        let uid = self.uid();
        let user = get_user_by_uid(uid)
            .map(|u| u.name().to_string_lossy().into_owned())
            .unwrap_or_else(|| uid.to_string());
        return user;
    }

    fn to_hr_group(&self) -> String {
        let gid = self.gid();
        let group = get_group_by_gid(gid)
            .map(|g| g.name().to_string_lossy().into_owned())
            .unwrap_or_else(|| gid.to_string());
        return group;
    }
}

