package app.atomofiron.searchboxapp.custom.view.menu

import androidx.annotation.DrawableRes
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.other.UniText

data class MenuItem(
    val id: Int,
    val label: UniText,
    val content: MenuItemContent,
    val enabled: Boolean = true,
    val longLabel: UniText? = null,
) {
    constructor(
        id: Int,
        title: UniText,
        @DrawableRes icon: Int,
        forwardable: Boolean = false,
        longLabel: UniText? = null,
    ) : this(id, title, MenuItemContent(icon, R.drawable.ic_forward_12.takeIf { forwardable }), longLabel = longLabel)

    fun copy(
        @DrawableRes icon: Int,
        enabled: Boolean = true,
    ): MenuItem {
        val content = when (content) {
            is MenuItemContent.Common -> content.copy(head = icon)
            is MenuItemContent.Dangerous -> content.copy(icon = icon)
        }
        return copy(enabled = enabled, content = content)
    }
}