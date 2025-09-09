package app.atomofiron.searchboxapp.screens.common.delegates

import app.atomofiron.common.util.Increment
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuItemContent
import app.atomofiron.searchboxapp.model.other.UniText

object Operations {
    val InstallApp = MenuItem(id = Increment(), UniText(R.string.install), R.drawable.ic_download)
    val LaunchApp = MenuItem(id = Increment(), UniText(R.string.launch), R.drawable.ic_play)
    val UseAs = MenuItem(id = Increment(), UniText(R.string.use_as), R.drawable.ic_puzzle)
    val Duplicate = MenuItem(id = Increment(), UniText(R.string.duplicate), R.drawable.ic_duplicate, forwardable = true)
    val CopyMove = MenuItem(id = Increment(), UniText(R.string.copy_move), R.drawable.ic_copy)
    val Create = MenuItem(id = Increment(), UniText(R.string.create_new), R.drawable.ic_create, longLabel = UniText(R.string.create_file_or_dir), forwardable = true)
    val Rename = MenuItem(id = Increment(), UniText(R.string.rename), R.drawable.ic_rename, forwardable = true)
    val OpenWith = MenuItem(id = Increment(), UniText(R.string.open_with), R.drawable.ic_open_outside)
    val Share = MenuItem(id = Increment(), UniText(R.string.share_files), R.drawable.ic_share)
    val CopyPath = MenuItem(id = Increment(), UniText(R.string.copy_path), R.drawable.ic_link)
    val Delete = MenuItem(id = Increment(), UniText(R.string.delete), MenuItemContent.Dangerous(R.drawable.ic_trashbox))
}
