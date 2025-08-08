package app.atomofiron.searchboxapp.model.preference;

enum class JoystickHaptic(val index: Int) {
    None(0),
    Lite(1),
    Double(2),
    Heavy(3),
    ;
    companion object Companion {
        fun index(index: Int) = entries[index % entries.size]
        fun bits(bits: Int, offset: Int) = entries[bits.shr(offset) % entries.size]
    }
    fun bits(offset: Int) = index shl offset
}
