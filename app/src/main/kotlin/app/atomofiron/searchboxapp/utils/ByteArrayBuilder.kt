package app.atomofiron.searchboxapp.utils

import java.nio.ByteBuffer
import kotlin.math.roundToInt

class ByteArrayBuilder(initialCapacity: Int = 256) {

    private var buffer = ByteArray(initialCapacity)
    var size = 0
        private set

    private fun ensureCapacity(newSize: Int) {
        if (newSize <= buffer.size) return
        var newCap = buffer.size
        while (newCap < newSize) newCap = newCap * 2
        buffer = buffer.copyOf(newCap)
    }

    fun append(b: Byte) {
        ensureCapacity((size * 1.1).roundToInt())
        buffer[size++] = b
    }

    fun append(bytes: ByteArray) {
        append(bytes, 0, bytes.size)
    }

    fun append(buffer: ByteBuffer): Int {
        val length = buffer.limit() - buffer.position()
        append(buffer.array(), buffer.position(), length)
        buffer.position(buffer.limit())
        return length
    }

    fun append(bytes: ByteArray, offset: Int, length: Int) {
        if (length <= 0) return
        ensureCapacity(size + length)
        bytes.copyInto(buffer, destinationOffset = size, startIndex = offset, endIndex = offset + length)
        size += length
    }

    fun clear() {
        size = 0
    }

    fun toByteArray() = buffer.sliceArray(0..<size)

    override fun toString() = "${this::class.java.simpleName}([${buffer.size}], size=$size)"
}
