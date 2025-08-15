package app.atomofiron.searchboxapp.screens.delegates

import app.atomofiron.common.util.Increment
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuItemContent
import app.atomofiron.searchboxapp.model.other.UniText

object Operations {
    val InstallApp = MenuItem(id = Increment(), UniText(R.string.install), R.drawable.ic_download)
    val LaunchApp = MenuItem(id = Increment(), UniText(R.string.launch), R.drawable.ic_play, outside = true)
    val UseAs = MenuItem(id = Increment(), UniText(R.string.use_as), R.drawable.ic_puzzle, outside = true)
    val Clone = MenuItem(id = Increment(), UniText(R.string.clone), R.drawable.ic_clone)
    val Create = MenuItem(id = Increment(), UniText(R.string.create_file_or_dir), R.drawable.ic_create)
    val Rename = MenuItem(id = Increment(), UniText(R.string.rename), R.drawable.ic_rename)
    val OpenWith = MenuItem(id = Increment(), UniText(R.string.open_with), R.drawable.ic_outside, outside = true)
    val Share = MenuItem(id = Increment(), UniText(R.string.share), R.drawable.ic_share, outside = true)
    val CopyPath = MenuItem(id = Increment(), UniText(R.string.copy_path), R.drawable.ic_link)
    val Delete = MenuItem(id = Increment(), UniText(R.string.delete), MenuItemContent.Dangerous)
}
