package app.atomofiron.searchboxapp.utils

import android.os.Looper
import app.atomofiron.common.util.UnreachableException
import app.atomofiron.common.util.extension.size
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

private val charBuf = CharBuffer.allocate(1024)
private val decoders = arrayOf(Charsets.UTF_8).associateWith {
    it.newDecoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE)
}

fun ByteArray.countChars(charset: Charset, range: IntRange): Int {
    if (!Looper.getMainLooper().isCurrentThread) {
        throw UnreachableException()
    }
    var count = 0
    val byteBuf = ByteBuffer.wrap(this, range.start, range.size)
    val decoder = decoders[charset]
    decoder ?: throw IllegalArgumentException("$charset doesn't exist in ${decoders.keys}")

    while (true) {
        val result = decoder.decode(byteBuf, charBuf, true)
        count += charBuf.position()
        charBuf.clear()
        when {
            result.isUnderflow -> break
            result.isOverflow -> continue
            result.isError -> result.throwException()
        }
    }
    val result = decoder.flush(charBuf)
    count += charBuf.position()
    charBuf.clear()
    decoder.reset()

    if (result.isError) {
        result.throwException()
    }
    return count
}
