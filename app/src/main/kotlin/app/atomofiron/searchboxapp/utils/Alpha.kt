package app.atomofiron.searchboxapp.utils

@Suppress("ConstPropertyName")
object Alpha {
    // todo replace everywhere with this
    const val Invisible = 0f
    const val Half = 0.5f
    const val Visible = 1f

    fun visible(value: Boolean) = if (value) Visible else Invisible
    fun enabled(value: Boolean) = if (value) Visible else Half
}