package app.atomofiron.searchboxapp.custom.view.dock

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class DockItem(
    val id: Id,
    val icon: Icon?,
    @StringRes val label: Int,
    val enabled: Boolean = true,
    val selected: Boolean = false,
    val children: DockItemChildren = DockItemChildren.Stub,
) {
    sealed interface Icon {
        @JvmInline
        value class ResId(@DrawableRes val resId: Int) : Icon
        @JvmInline
        value class Res(val drawable: Drawable) : Icon
    }
    interface Id {
        val value: Long

        @JvmInline
        private value class Digit(override val value: Long) : Id

        companion object {
            operator fun invoke(value: Long): Id = Digit(value)
            val Undefined: Id = Digit(0L)
        }
    }

    constructor(
        id: Id,
        @DrawableRes icon: Int,
        @StringRes label: Int,
        enabled: Boolean = true,
        selected: Boolean = false,
        children: DockItemChildren = DockItemChildren.Stub,
    ) : this(id, Icon.ResId(icon), label, enabled, selected, children)

    constructor(
        id: Id,
        icon: Drawable,
        @StringRes label: Int,
        enabled: Boolean = true,
        selected: Boolean = false,
        children: DockItemChildren = DockItemChildren.Stub,
    ) : this(id, Icon.Res(icon), label, enabled, selected, children)

    fun with(@DrawableRes resId: Int) = copy(icon = Icon.ResId(resId))

    fun with(drawable: Drawable) = copy(icon = Icon.Res(drawable))
}
