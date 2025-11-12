use std::process::Command;
use crate::api::protocol::SuCmd;

impl SuCmd {

    pub fn command(&self, arg: &String) -> Command {
        let mut args = self.cmd.split(' ').filter(|s| !s.is_empty());
        let cmd = args.nth(0).unwrap_or("su");
        let mut command = Command::new(cmd);
        command.args(args).arg(arg);
        return command;
    }
}