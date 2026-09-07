package app.atomofiron.searchboxapp.custom.view.dock.item

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.atomofiron.searchboxapp.utils.Id

data class DockItem(
    val id: Id,
    val icon: Icon? = null,
    val label: Label? = null,
    val enabled: Boolean = true,
    val clickable: Boolean? = null, // null = by this.enabled
    val selected: Boolean = false,
    val primary: Boolean = false,
    val progress: Boolean = false,
    val notice: Notice? = null,
    val children: DockItemChildren = DockItemChildren.Empty,
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
            val Empty = Value("")
            operator fun invoke(@StringRes resId: Int) = Res(resId)
            operator fun invoke(value: String?) = when (value) {
                null -> null
                Empty.value -> Empty
                else -> Value(value)
            }
        }
    }
    enum class Notice(val alert: Boolean) {
        Normal(false),
        Alert(true),
    }

    fun shouldBeClickable() = clickable ?: enabled

    fun with(@DrawableRes resId: Int) = copy(icon = Icon.Res(resId))

    fun with(drawable: Drawable) = copy(icon = Icon.Value(drawable))

    fun withOutChildren() = copy(children = DockItemChildren.Empty)
}
