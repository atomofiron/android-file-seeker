package app.atomofiron.searchboxapp.custom.view.menu

import app.atomofiron.common.util.Increment

private val increment = Increment.new()

enum class MenuItemType(val viewType: Int) {
    Common(increment()),
    Dangerous(increment()),
    ;
    companion object {
        operator fun invoke(viewType: Int) = entries[viewType]
    }
}