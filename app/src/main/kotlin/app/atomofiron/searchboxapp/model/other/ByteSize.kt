package app.atomofiron.searchboxapp.model.other

fun ULong?.toByteSize() = ByteSize(this ?: ByteSize.UNDEFINED)

fun Long?.toByteSize() = ByteSize(this?.toULong() ?: ByteSize.UNDEFINED)

@JvmInline
value class ByteSize(val value: ULong = UNDEFINED) {
    companion object {
        const val UNDEFINED = ULong.MIN_VALUE
    }

    fun resolve(): ULong? = if (value == UNDEFINED) null else value

    fun persistable(): Long = value.toLong()

    override fun toString(): String = resolve()?.toString() ?: ""
}