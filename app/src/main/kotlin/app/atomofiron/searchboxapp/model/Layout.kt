package app.atomofiron.searchboxapp.model

@JvmInline
value class Layout private constructor(val value: Int = 0) {
    enum class Ground(val isBottom: Boolean = false) {
        Left, Bottom(true), Right,
    }
    companion object {
        private const val JOYSTICK =    0b0001
        private const val GROUND_MASK = 0b0110
        private const val LEFT =        0b0010
        private const val RIGHT =       0b0100
        private const val BOTTOM =      0b0110
        private const val RTL =         0b1000

        private fun get(ground: Ground, withJoystick: Boolean, rtl: Boolean): Int {
            var value = if (withJoystick) JOYSTICK else 0
            value = when (ground) {
                Ground.Left -> value or LEFT
                Ground.Right -> value or RIGHT
                Ground.Bottom -> value or BOTTOM
            }
            return if (rtl) (value or RTL) else value
        }
    }

    val withJoystick: Boolean get() = (value and JOYSTICK) == JOYSTICK
    val isLeft: Boolean get() = (value and GROUND_MASK) == LEFT
    val isRight: Boolean get() = (value and GROUND_MASK) == RIGHT
    val isBottom: Boolean get() = (value and GROUND_MASK) == BOTTOM
    val isRtl: Boolean get() = (value and RTL) == RTL
    val isStart: Boolean get() = if (isRtl) isRight else isLeft
    val isEnd: Boolean get() = if (isRtl) isLeft else isRight
    val isWide: Boolean get() = !isBottom
    val ground: Ground get() = when {
        isLeft -> Ground.Left
        isRight -> Ground.Right
        else -> Ground.Bottom
    }

    constructor(side: Ground, withJoystick: Boolean, rtl: Boolean) : this(get(side, withJoystick, rtl))

    override fun toString(): String = when {
        isLeft -> "Layout.Left(withJoystick = $withJoystick)"
        isRight -> "Layout.Right(withJoystick = $withJoystick)"
        else -> "Layout.Bottom(withJoystick = $withJoystick)"
    }
}
