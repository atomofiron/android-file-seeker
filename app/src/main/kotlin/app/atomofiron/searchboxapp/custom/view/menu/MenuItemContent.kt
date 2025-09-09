package app.atomofiron.searchboxapp.custom.view.menu

import androidx.annotation.DrawableRes

sealed class MenuItemContent(val cells: Int) {

    data class Common(
        @DrawableRes val head: Int,
        @DrawableRes val tail: Int? = null,
    ) : MenuItemContent(cells = 1)

    data class Dangerous(
        @DrawableRes val icon: Int,
    ) : MenuItemContent(cells = 2)
    
    companion object {
        operator fun invoke(
            head: Int,
            tail: Int? = null,
        ) = Common(head, tail)
    }
}
