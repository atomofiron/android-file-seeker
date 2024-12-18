package app.atomofiron.searchboxapp.utils

@Suppress("ConstPropertyName")
object Alpha {
    const val Invisible = 0f
    const val Half = 0.5f
    const val Visible = 1f

    const val Level5 = 12
    const val Level10 = 25
    const val Level12 = 31
    const val Level30 = 80
    const val Level50 = 128
    const val Level67 = 170
    const val Level80 = 200
    const val Level90 = 225

    const val InvisibleInt = 0
    const val HalfInt = 0x80
    const val VisibleInt = 0xff

    const val RippleInt = HalfInt

    fun visible(value: Boolean) = if (value) Visible else Invisible
    fun enabled(value: Boolean) = if (value) Visible else Half
}

fun Float.toIntAlpha(): Int = (this * Alpha.VisibleInt).toInt().coerceIn(Alpha.InvisibleInt, Alpha.VisibleInt)