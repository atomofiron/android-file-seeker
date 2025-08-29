package app.atomofiron.searchboxapp.model.preference

import android.graphics.Color
import androidx.core.graphics.ColorUtils

private const val INV_FOR_DARK  =  1 shl 24
private const val INV_GLOWING =    1 shl 25
private const val OVERRIDE_COLOR = 1 shl 26
private const val HAPTIC_OFFSET = 27
private const val BYTE = 256
private const val FF = 255

private val hsl = FloatArray(3)

data class JoystickComposition(
    val invForDark: Boolean,
    val invGlowing: Boolean,
    val overrideColor: Boolean,
    val haptic: JoystickHaptic,
    val red: Int,
    val green: Int,
    val blue: Int,
) {
    companion object {
        val DEFAULT = 0xff5252 + INV_FOR_DARK + JoystickHaptic.Heavy.bits(HAPTIC_OFFSET)
        val Default = JoystickComposition(DEFAULT)
    }

    constructor(flags: Int) : this(
        invForDark = (flags and INV_FOR_DARK) == INV_FOR_DARK,
        invGlowing = (flags and INV_GLOWING) == INV_GLOWING,
        overrideColor = (flags and OVERRIDE_COLOR) == OVERRIDE_COLOR,
        haptic = JoystickHaptic.bits(flags, HAPTIC_OFFSET),
        red = flags / BYTE / BYTE % BYTE,
        green = flags / BYTE % BYTE,
        blue = flags % BYTE,
    )

    val data: Int

    init {
        var data = rgb()
        if (invForDark) {
            data += INV_FOR_DARK
        }
        if (invGlowing) {
            data += INV_GLOWING
        }
        if (overrideColor) {
            data += OVERRIDE_COLOR
        }
        data += haptic.bits(HAPTIC_OFFSET)
        this.data = data
    }

    fun color(isDark: Boolean): Int = when {
        isDark && invForDark -> inverseColor()
        else -> rgba()
    }

    fun glow(color: Int): Int = when {
        invGlowing -> inverseColor(color)
        else -> color
    }

    fun colorText(channel: Int): String {
        val hex = Integer.toHexString(channel)
        val builder = StringBuilder(hex)
        while (builder.length < 2) {
            builder.insert(0, '0')
        }
        return builder.toString()
    }

    fun colorText(): String {
        val color = rgb()
        val hex = Integer.toHexString(color)
        val builder = StringBuilder(hex)
        while (builder.length < 6) {
            builder.insert(0, '0')
        }
        builder.insert(0, '#')
        return builder.toString()
    }

    private fun rgb(): Int = Color.argb(0, red, green, blue)

    private fun rgba(): Int = Color.argb(FF, red, green, blue)

    private fun inverseColor(color: Int = rgba()): Int {
        ColorUtils.colorToHSL(color, hsl)
        hsl[2] = 1f - hsl[2]
        return ColorUtils.HSLToColor(hsl)
    }
}