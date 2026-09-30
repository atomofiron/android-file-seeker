package app.atomofiron.searchboxapp.model.textviewer

import app.atomofiron.common.util.extension.hash
import java.nio.charset.Charset

data class TextLine(
    val offset: ULong,
    val text: ByteArray,
    val charset: Charset,
    val skipEnd: Int,
) {
    val length get() = text.size
    val end get() = offset + length.toULong()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is TextLine -> false
        other.offset != offset -> false
        other.skipEnd != skipEnd -> false
        other.charset != charset -> false
        else -> text.contentEquals(other.text)
    }

    override fun hashCode(): Int = hash(offset, length, charset, text, skipEnd)

    override fun toString(): String = "${this::class.java.simpleName}(offset=$offset, text=[$length], charset=${charset.name()}, skipEnd=$skipEnd)"
}
