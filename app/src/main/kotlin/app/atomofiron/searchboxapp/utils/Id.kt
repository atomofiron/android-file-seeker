package app.atomofiron.searchboxapp.utils

interface Id {
    val value: Long

    @JvmInline
    private value class Digit(override val value: Long) : Id

    open class Auto : Id {
        override val value = next++
    }

    interface Factory {
        fun nextId(): Id = Digit(next++)
    }

    companion object {
        val Undefined: Id = Digit(-1L)
        private var next = Int.MAX_VALUE.toLong()

        operator fun invoke(value: Int): Id = Digit(value.toLong())
        operator fun invoke(value: Long): Id = Digit(value)
    }
}