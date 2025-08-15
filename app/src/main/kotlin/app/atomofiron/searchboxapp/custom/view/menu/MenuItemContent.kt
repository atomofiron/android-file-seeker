package app.atomofiron.searchboxapp.custom.view.menu

import androidx.annotation.DrawableRes

sealed interface MenuItemContent {

    data class Common(
        @DrawableRes val head: Int,
        @DrawableRes val tail: Int? = null,
    ) : MenuItemContent

    data object Dangerous : MenuItemContent
    
    companion object {
        operator fun invoke(
            head: Int,
            tail: Int? = null,
        ) = Common(head, tail)
    }
}
