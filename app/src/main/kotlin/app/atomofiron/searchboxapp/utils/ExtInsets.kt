package app.atomofiron.searchboxapp.utils

import androidx.core.graphics.Insets
import lib.atomofiron.insets.ExtendedWindowInsets
import lib.atomofiron.insets.TypeSet

object ExtType : ExtendedWindowInsets.Type() {
    val dock = define("dock")
    val rail = define("rail")
    val navigation = define("navigation")
    val joystickTop = define("joystickTop")
    val joystickFlank = define("joystickFlank")
    val joystickBottom = define("joystickBottom")
    val curtain = define("curtain")

    inline operator fun invoke(block: ExtType.() -> TypeSet): TypeSet = ExtType.block()
}

// associate your custom type with ExtendedWindowInsets
operator fun ExtendedWindowInsets.invoke(block: ExtType.() -> TypeSet): Insets = get(ExtType.block())

