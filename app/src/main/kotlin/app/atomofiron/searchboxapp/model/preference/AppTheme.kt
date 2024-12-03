package app.atomofiron.searchboxapp.model.preference

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import java.util.*

sealed class AppTheme(
    val name: String,
    val deepBlack: Boolean,
) {

    class System(deepBlack: Boolean) : AppTheme(NAME_SYSTEM, deepBlack)
    class Light(deepBlack: Boolean) : AppTheme(NAME_LIGHT, deepBlack)
    class Dark(deepBlack: Boolean) : AppTheme(NAME_DARK, deepBlack)

    override fun equals(other: Any?) = when {
        other == null -> false
        other !is AppTheme -> false
        other.name != name -> false
        other is Light -> true
        else -> other.deepBlack == deepBlack
    }

    override fun hashCode(): Int = Objects.hash(this::class, deepBlack)

    companion object {

        private const val NAME_SYSTEM = "system"
        private const val NAME_LIGHT = "light"
        private const val NAME_DARK = "dark"

        fun defaultName(): String = when {
            SDK_INT >= Q -> NAME_SYSTEM
            else -> NAME_LIGHT
        }

        fun fromString(name: String?, deepBlack: Boolean = false) = when (name) {
            NAME_SYSTEM -> System(deepBlack)
            NAME_LIGHT -> Light(deepBlack)
            NAME_DARK -> Dark(deepBlack)
            else -> when {
                SDK_INT >= Q -> System(deepBlack)
                else -> Light(deepBlack)
            }
        }
    }
}