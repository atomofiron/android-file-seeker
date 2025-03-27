package app.atomofiron.searchboxapp.model.preference

import android.graphics.Color

data class JoystickComposition(
    val invForDark: Boolean,
    val invGlowing: Boolean,
    val overrideTheme: Boolean,
    val withHaptic: Boolean,
    val red: Int,
    val green: Int,
    val blue: Int,
) {
    companion object {
        private const val INV_FOR_DARK  =  0x01000000
        private const val INV_GLOWING =    0x02000000
        private const val OVERRIDE_THEME = 0x04000000
        private const val WITH_HAPTIC =    0x08000000
        private const val BYTE = 256
        private const val FF = 255

        const val DEFAULT = 16732754 + WITH_HAPTIC // #ff5252 + WITH_HAPTIC
        val Default = JoystickComposition(DEFAULT)
    }

    constructor(flags: Int) : this(
        invForDark = (flags and INV_FOR_DARK) == INV_FOR_DARK,
        invGlowing = (flags and INV_GLOWING) == INV_GLOWING,
        overrideTheme = (flags and OVERRIDE_THEME) == OVERRIDE_THEME,
        withHaptic = (flags and WITH_HAPTIC) == WITH_HAPTIC,
        red = flags / BYTE / BYTE % BYTE,
        green = flags / BYTE % BYTE,
        blue = flags % BYTE,
    )

    val data: Int

    init {
        var data = red * BYTE * BYTE + green * BYTE + blue
        if (invForDark) {
            data += INV_FOR_DARK
        }
        if (invGlowing) {
            data += INV_GLOWING
        }
        if (overrideTheme) {
            data += OVERRIDE_THEME
        }
        if (withHaptic) {
            data += WITH_HAPTIC
        }
        this.data = data
    }

    fun color(isDark: Boolean): Int = when (isDark && invForDark) {
        true -> Color.argb(FF, FF - red, FF - green, FF - blue)
        else -> Color.argb(FF, red, green, blue)
    }

    fun glow(isDark: Boolean): Int = when ((isDark && invForDark) xor invGlowing) {
        true -> Color.argb(FF, FF - red, FF - green, FF - blue)
        else -> Color.argb(FF, red, green, blue)
    }

    fun glow(isDark: Boolean, color: Int): Int = when {
        !invGlowing -> color
        !isDark -> color
        else -> {
            val alpha = Color.alpha(color)
            val red = FF - Color.red(color)
            val green = FF - Color.green(color)
            val blue = FF - Color.blue(color)
            Color.argb(alpha, red, green, blue)
        }
    }

    fun colorText(): String {
        val builder = StringBuilder("#")
        val color = red * BYTE * BYTE + green * BYTE + blue
        val hex = Integer.toHexString(color)
        for (i in 0..(5 - hex.length)) {
            builder.append("0")
        }
        builder.append(hex)
        return builder.toString()
    }
}