package app.atomofiron.searchboxapp.utils

class ByteArrayBuffer(initialCapacity: Int = 256) {

    private var buffer = ByteArray(initialCapacity)
    var size = 0
        private set

    val lastIndex get() = size.dec()

    val indices: IntRange get() = IntRange(0, lastIndex)

    var cursor = 0

    private fun ensureCapacity(newSize: Int) {
        if (newSize <= buffer.size) return
        var newCap = buffer.size
        while (newCap < newSize) newCap *= 2
        buffer = buffer.copyOf(newCap)
    }

    operator fun get(index: Int) = buffer[index]

    fun getOrNull(index: Int) = buffer.getOrNull(index)

    fun append(b: Byte) {
        ensureCapacity(size.inc())
        buffer[size++] = b
    }

    fun append(bytes: ByteArray) {
        ensureCapacity(size + bytes.size)
        bytes.copyInto(
            buffer,
            destinationOffset = size,
            startIndex = 0,
            endIndex = bytes.size,
        )
        size += bytes.size
    }

    fun clear() {
        size = 0
        cursor = 0
    }

    fun isEmpty() = size == 0

    fun consume(length: Int): ByteArray {
        if (length > size || length > cursor) {
            throw IllegalArgumentException("$length/$cursor/$size")
        }
        val bytes = buffer.sliceArray(0..<length)
        buffer.copyInto(
            destination = buffer,
            destinationOffset = 0,
            startIndex = length,
            endIndex = size,
        )
        size -= length
        cursor -= length
        return bytes
    }

    fun copyInto(destination: ByteArray, offset: Int) {
        if (offset + destination.size > size) {
            throw IllegalArgumentException("($offset+${destination.size})/$size")
        }
        buffer.copyInto(
            destination = destination,
            destinationOffset = 0,
            startIndex = offset,
            endIndex = offset + destination.size,
        )
    }

    override fun toString() = "${this::class.java.simpleName}([${buffer.size}], size=$size)"
}
