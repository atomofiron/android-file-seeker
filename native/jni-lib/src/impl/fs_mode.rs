
pub trait HumanReadableMode {
    fn to_hr_mode(&self) -> String;
}

impl HumanReadableMode for u32 {

    fn to_hr_mode(&self) -> String {
        #[cfg(any(target_arch = "aarch64", target_arch = "x86_64"))]
        let mode = *self;
        #[cfg(any(target_arch = "arm", target_arch = "x86"))]
        let mode = *self as u16;
        let file_type = match mode & libc::S_IFMT {
            libc::S_IFDIR => 'd',
            libc::S_IFLNK => 'l',
            libc::S_IFCHR => 'c',
            libc::S_IFBLK => 'b',
            libc::S_IFSOCK => 's',
            libc::S_IFIFO => 'p',
            _ => '-',
        };
        let mut perms = String::new();
        perms.push(file_type);

        perms.push(if mode & libc::S_IRUSR != 0 { 'r' } else { '-' });
        perms.push(if mode & libc::S_IWUSR != 0 { 'w' } else { '-' });
        perms.push(if mode & libc::S_IXUSR != 0 { 'x' } else { '-' });

        perms.push(if mode & libc::S_IRGRP != 0 { 'r' } else { '-' });
        perms.push(if mode & libc::S_IWGRP != 0 { 'w' } else { '-' });
        perms.push(if mode & libc::S_IXGRP != 0 { 'x' } else { '-' });

        perms.push(if mode & libc::S_IROTH != 0 { 'r' } else { '-' });
        perms.push(if mode & libc::S_IWOTH != 0 { 'w' } else { '-' });
        perms.push(if mode & libc::S_IXOTH != 0 { 'x' } else { '-' });

        return perms;
    }
}
