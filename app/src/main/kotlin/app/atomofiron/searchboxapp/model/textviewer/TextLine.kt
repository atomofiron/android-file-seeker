package app.atomofiron.searchboxapp.model.textviewer

class TextLine(
    val offset: Int,
    val text: ByteArray,
) {
    val length get() = text.size

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is TextLine -> false
        other.offset != offset -> false
        else -> text.contentEquals(other.text)
    }

    override fun hashCode(): Int {
        var result = offset
        result = 31 * result + length
        result = 31 * result + text.contentHashCode()
        return result
    }

    override fun toString(): String = "${this::class.java.simpleName}(byteOffset=$offset, text=[$length])"
}
