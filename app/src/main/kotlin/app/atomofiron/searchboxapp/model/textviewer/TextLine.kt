package app.atomofiron.searchboxapp.model.textviewer

import app.atomofiron.common.util.extension.hash
import java.nio.charset.Charset

class TextLine(
    val offset: ULong,
    val text: ByteArray,
    val charset: Charset,
) {
    val length get() = text.size
    val end get() = offset + length.toULong()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is TextLine -> false
        other.offset != offset -> false
        other.charset != charset -> false
        else -> text.contentEquals(other.text)
    }

    override fun hashCode(): Int = hash(offset, length, charset, text)

    override fun toString(): String = "${this::class.java.simpleName}(offset=$offset, text=[$length], charset=${charset.name()})"
}
