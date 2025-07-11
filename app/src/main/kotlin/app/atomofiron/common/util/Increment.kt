package app.atomofiron.common.util

interface Increment {
    companion object : Increment by Impl() {
        private class Impl(private var current: Int = 0) : Increment {
            override fun invoke(): Int = current++
        }
        fun new(first: Int = 0): Increment = Impl(first)
    }
    operator fun invoke(): Int
    fun nextInt(): Int = invoke()
}
