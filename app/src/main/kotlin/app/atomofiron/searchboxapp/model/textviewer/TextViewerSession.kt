package app.atomofiron.searchboxapp.model.textviewer

import app.atomofiron.common.util.GrowingList
import app.atomofiron.searchboxapp.di.dependencies.store.DefaultCharset
import app.atomofiron.searchboxapp.di.dependencies.store.DefaultCharsetName
import app.atomofiron.searchboxapp.di.dependencies.store.SupportedCharsets
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.LocalSearchTask
import app.atomofiron.searchboxapp.utils.ByteArrayBuffer
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNode
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNodeError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uniffi.native_lib.FileReader
import uniffi.native_lib.ReadResult
import uniffi.native_lib.SupportedCharset
import java.io.Closeable
import java.nio.charset.Charset
import kotlin.text.Charsets.UTF_16
import kotlin.text.Charsets.UTF_16BE
import kotlin.text.Charsets.UTF_16LE
import kotlin.uuid.Uuid

private const val CR: Byte = 0x0D
private const val LF: Byte = 0x0A
private const val FF: Byte = 0xFF.toByte()
private const val FE: Byte = 0xFE.toByte()
private const val NULL: Byte = 0x00
private val window = ByteArray(4)

class TextViewerSession(
    private val input: FileReader,
    private val length: ULong,
    ref: NodeRef,
    private val charsets: SupportedCharsets,
    charset: String,
) : Closeable {

    private val buffer = ByteArrayBuffer()
    private var byteCount = 0uL
    var charset: Charset = Charset.forName( charset)
        private set(value) {
            field = value
            exactCharset = value.name()
            charsetName.value = value.name()
        }
    val charsetName: StateFlow<String>
        field = MutableStateFlow(this.charset.name())
    var exactCharset: String = this.charset.name() // UTF-16 -> UTF-16LE or UTF-16BE
        private set
    private var utf8byteCount = 0uL
    var isFullyRead = false
        private set

    val mutex = Mutex()
    val item: StateFlow<Node>
        field = MutableStateFlow(ref.toNode())
    val error: StateFlow<NodeError?>
        field = MutableStateFlow<NodeError?>(null)
    private var lineList = GrowingList<TextLine>()
    val lines: StateFlow<List<TextLine>>
        field = MutableStateFlow(listOf())
    val loading = MutableStateFlow(false)
    val reading: StateFlow<Reading>
        field = MutableStateFlow<Reading>(Reading.Stub)
    val tasks: StateFlow<List<LocalSearchTask>>
        field = MutableStateFlow(listOf())

    fun updateItem(item: Node) {
        this.item.value = item
    }

    suspend fun setCharset(name: String): Boolean {
        val new = charsets.get(name)
            .takeIf { it != this.charset }
            ?: return false
        return mutex.withLock {
            charset = Charset.forName(new.name)
            utf8byteCount = 0uL
            val newList = GrowingList<TextLine>(lineList.size)
            lineList.forEachIndexed { index, it ->
                val skip = if (index == 0) 0 else it.skip
                val new = it.text.toTextLine(newList.firstOrNull(), skip)
                newList.add(index, new)
            }
            lineList = newList
            lines.value = newList.fetch()
            true
        }
    }

    private fun SupportedCharsets.get(name: String): SupportedCharset = list.value
        .find { it.name == name }
        ?: SupportedCharset(DefaultCharsetName)

    fun getOrNull(uuid: Uuid) = tasks.value.find { it.uuid == uuid }

    suspend fun tasks(action: suspend MutableList<LocalSearchTask>.() -> Unit) = mutex.withLock {
        tasks.run {
            value = value.toMutableList()
                .apply { action() }
        }
    }

    suspend fun textLines(action: suspend GrowingList<TextLine>.() -> Unit) = mutex.withLock {
        lines.run {
            lineList.action()
            value = lineList.fetch()
        }
    }

    override fun close() = input.close()

    fun readLine(): TextLine? {
        if (isFullyRead && buffer.isEmpty()) {
            return null
        }
        var length = 0
        var skip = 0
        while (length == 0) {
            val pair = buffer.findEndOfLine(0)
            length = pair.first
            skip = pair.second
            when (length) {
                0 if isFullyRead -> return null
                0 -> buffer.readMore()
            }
        }
        val text = buffer.consume(length)
        return text.toTextLine(first = lineList.firstOrNull(), skip = skip)
    }

    fun ByteArray.toTextLine(first: TextLine?, skip: Int): TextLine {
        val (charset, skip) = when {
            charset == UTF_16 && first != null -> first.charset to skip
            charset == UTF_16 -> bomToUtf16X()
                ?.also { exactCharset = it.name() }
                ?.let { it to 2 }
                ?: (charset to skip)
            first != null || !charset.isOneOfUtf16() -> charset to skip
            charset == bomToUtf16X() -> charset to 2
            else -> charset to skip
        }
        val offset = countBytes(charset)
        return TextLine(offset, skip, this, charset)
    }

    private fun ByteArray.countBytes(charset: Charset): ULong {
        val string = String(this, charset)
        val bytes = string.toByteArray(DefaultCharset)
        val offset = utf8byteCount
        utf8byteCount += bytes.size.toULong()
        return offset
    }

    private fun ByteArrayBuffer.readMore() {
        when (val result = input.next()) {
            is ReadResult.Ok -> {
                append(result.v1)
                updateReading(result.v1.size.toULong())
            }
            is ReadResult.End -> isFullyRead = true
            is ReadResult.Err -> {
                isFullyRead = true
                error.value = result.v1.toNodeError()
            }
        }
    }

    private fun updateReading(increment: ULong) {
        byteCount += increment
        var loaded = byteCount
        var length = length
        var denominator = 1
        while (length > Int.MAX_VALUE.toULong()) {
            length /= 2uL
            loaded /= 2uL
            denominator *= 2
        }
        reading.value = Reading(loaded.toInt(), length.toInt(), denominator)
    }

    private fun ByteArrayBuffer.findEndOfLine(orElse: Int): Pair<Int, Int> {
        val w = window
        val skip = cursor
        for (i in skip..<size) {
            if (i <= size - 4) {
                copyInto(w, i)
            } else if (!isFullyRead) {
                return orElse to 0 // need more bytes
            } else {
                w[0] = get(i)
                w[1] = getOrNull(i + 1) ?: NULL
                w[2] = getOrNull(i + 2) ?: NULL
                w[3] = NULL
            }
            val even = (i and 1) == 0
            val bytes = when {
                even && w[0] == CR && w[1] == NULL && w[2] == LF && w[3] == NULL -> 4
                even && w[0] == NULL && w[1] == CR && w[2] == NULL && w[3] == LF -> 4
                even && w[0] == CR && w[1] == NULL -> 2
                even && w[0] == NULL && w[1] == CR -> 2
                even && w[0] == LF && w[1] == NULL -> 2
                even && w[0] == NULL && w[1] == LF -> 2
                w[0] == CR && w[1] == LF -> 2
                w[0] == CR -> 1
                w[0] == LF -> 1
                else -> continue
            }
            cursor = (i + bytes).coerceAtMost(size)
            return i to skip
        }
        return when {
            isFullyRead -> size to skip // the last text line
                .also { cursor = size }
            else -> orElse to 0
        }
    }
}

private fun ByteArray.bomToUtf16X() = when {
    size < 2 -> null
    get(0) == FF && get(1) == FE -> UTF_16LE
    get(0) == FE && get(1) == FF -> UTF_16BE
    else -> null
}

private fun Charset.isOneOfUtf16() = this == UTF_16 || this == UTF_16LE || this == UTF_16BE

