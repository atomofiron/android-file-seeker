package app.atomofiron.searchboxapp.custom.view.dock.item

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class DockItem(
    val id: Id,
    val icon: Icon? = null,
    val label: Label? = null,
    val enabled: Boolean = true,
    val selected: Boolean = false,
    val children: DockItemChildren = DockItemChildren.Stub,
) {
    sealed interface Icon {
        @JvmInline
        value class Res(@DrawableRes val resId: Int) : Icon
        @JvmInline
        value class Value(val drawable: Drawable) : Icon

        companion object {
            operator fun invoke(@DrawableRes resId: Int) = Res(resId)
            operator fun invoke(value: Drawable) = Value(value)
        }
    }
    sealed interface Label {
        @JvmInline
        value class Res(@StringRes val resId: Int) : Label
        @JvmInline
        value class Value(val value: String) : Label

        companion object {
            operator fun invoke(@StringRes resId: Int) = Res(resId)
            operator fun invoke(value: String?) = value?.let { Value(it) }
        }
    }
    interface Id {
        val value: Long

        @JvmInline
        private value class Digit(override val value: Long) : Id

        companion object {
            operator fun invoke(value: Long): Id = Digit(value)
            val Undefined: Id = Digit(-1L)
        }
    }

    fun with(@DrawableRes resId: Int) = copy(icon = Icon.Res(resId))

    fun with(drawable: Drawable) = copy(icon = Icon.Value(drawable))
}
