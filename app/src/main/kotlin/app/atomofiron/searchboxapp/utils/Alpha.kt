package app.atomofiron.searchboxapp.utils

object Alpha {
    const val INVISIBLE = 0f
    const val SMALL = 0.1f
    const val VODKA = 0.4f
    const val HALF = 0.5f
    const val VISIBLE = 1f

    const val LEVEL_5 = 12
    const val LEVEL_10 = 25
    const val LEVEL_12 = 31
    const val LEVEL_30 = 80
    const val LEVEL_50 = 128
    const val LEVEL_67 = 170
    const val LEVEL_80 = 200
    const val LEVEL_90 = 225

    const val INVISIBLE_INT = 0
    const val VODKA_INT = 0x66
    const val HALF_INT = 0x80
    const val VISIBLE_INT = 0xff

    const val RIPPLE_INT = HALF_INT

    fun visible(value: Boolean) = if (value) VISIBLE else INVISIBLE
    fun visibleInt(value: Boolean) = if (value) VISIBLE_INT else INVISIBLE_INT
    fun enabled(value: Boolean) = if (value) VISIBLE else HALF
    fun enabledInt(value: Boolean) = if (value) VISIBLE_INT else HALF_INT
    fun vodkaInt(value: Boolean) = if (value) VODKA_INT else VISIBLE_INT
}

fun Float.toIntAlpha(): Int = (this * Alpha.VISIBLE_INT).toInt().coerceIn(Alpha.INVISIBLE_INT, Alpha.VISIBLE_INT)