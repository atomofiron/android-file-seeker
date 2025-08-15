package app.atomofiron.searchboxapp.custom.view.menu

import androidx.annotation.DrawableRes
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.other.UniText

data class MenuItem(
    val id: Int,
    val label: UniText,
    val content: MenuItemContent,
    val enabled: Boolean = true,
) {
    constructor(
        id: Int,
        title: UniText,
        @DrawableRes icon: Int,
        outside: Boolean = false,
    ) : this(id, title, MenuItemContent(icon, R.drawable.ic_outside.takeIf { outside }))
}