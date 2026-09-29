package app.atomofiron.searchboxapp.model.textviewer

import app.atomofiron.common.util.GrowingList
import app.atomofiron.searchboxapp.di.dependencies.store.SupportedCharsets
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.LocalSearchTask
import app.atomofiron.searchboxapp.utils.ByteArrayBuilder
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
import java.nio.ByteBuffer
import java.nio.charset.Charset
import kotlin.uuid.Uuid

private const val BUFFER_SIZE = 8 * 1024
private const val CR: Byte = 0x0D
private const val LF: Byte = 0x0A
private const val NULL: Byte = 0x00

class TextViewerSession(
    private val input: FileReader,
    private val length: ULong,
    ref: NodeRef,
    private val charsets: SupportedCharsets,
    charset: String?,
) : Closeable {

    private var dual = false
    private val byteStep get() = if (dual) 2 else 1
    var charset = Charsets.UTF_8
        private set

    private var bytes = ByteArray(BUFFER_SIZE)
    private var byteBuf = ByteBuffer.wrap(bytes)
    private val lineBuilder = ByteArrayBuilder()
    private var byteCount = 0uL
        set(value) {
            field = value
            updateReading(value)
        }
    private var utf8byteCount = 0uL
    private var afterCr = false
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

    init {
        val charset = charsets.get(charset)
        this.charset = charset?.name
            ?.let{ Charset.forName(it) }
            ?: this.charset
        dual = charset?.dual ?: dual
        byteBuf.limit(0)
    }

    fun updateItem(item: Node) {
        this.item.value = item
    }

    suspend fun setCharset(name: String?): Boolean {
        val new = charsets.get(name)
            .takeIf { it != this.charset }
            ?: return false
        return mutex.withLock {
            charset = Charset.forName(new.name)
            dual = new.dual
            utf8byteCount = 0uL
            val newList = GrowingList<TextLine>(lineList.size)
            lineList.forEachIndexed { index, it ->
                val offset = it.text.countUtf8bytes(charset)
                newList.add(index, TextLine(offset, it.text, charset))
            }
            lineList = newList
            lines.value = newList.fetch()
            true
        }
    }

    private fun SupportedCharsets.get(name: String?): SupportedCharset? = list.value
        .takeIf { name != null }
        ?.find { it.name == name }

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
        if (isFullyRead) {
            return null
        }
        while (true) when {
            isFullyRead -> break
            !byteBuf.hasRemaining() -> fillBuffer()
                .also { isFullyRead = it }
            collect() -> skipCrLf()
                .also { break }
        }
        val text = lineBuilder.toByteArray()
        lineBuilder.clear()
        val offset = text.countUtf8bytes(charset)
        return TextLine(offset, text, charset)
    }

    private fun ByteArray.countUtf8bytes(charset: Charset): ULong {
        val string = String(this, charset)
        val bytes = string.toByteArray(Charsets.UTF_8)
        val offset = utf8byteCount
        utf8byteCount += bytes.size.inc().toULong()
        return offset
    }

    /** @return true if EOF */
    private fun fillBuffer(): Boolean {
        byteBuf.clear()
        return when (val result = input.next()) {
            is ReadResult.Ok -> {
                byteBuf.put(result.v1).flip()
                if (afterCr && result.v1.firstOrNull() == LF) {
                    byteCount++
                    byteBuf.position(1)
                }
                afterCr = false
                false
            }
            is ReadResult.End -> byteBuf.limit(0)
                .let { true }
            is ReadResult.Err -> {
                byteBuf.limit(0)
                error.value = result.v1.toNodeError()
                true
            }
        }
    }

    /** @return count of skipped bytes */
    private fun skipCrLf(): Int { // todo skip UTF-16
        if (!byteBuf.hasRemaining()) {
            return 0
        }
        val next = byteBuf.get(byteBuf.position())
        val afterNext = when (byteBuf.remaining()) {
            1 -> null
            else -> byteBuf.get(byteBuf.position().inc())
        }
        val skip = when {
            next != LF && next != CR -> 0
            byteBuf.remaining() == 1 -> 1.also {
                afterCr = next == CR
            }
            next == CR && afterNext == LF -> 2 // skip \r\n
            else -> 1 // skip \r or \n
        }
        byteCount += skip.toULong()
        byteBuf.position(byteBuf.position() + skip)
        return skip
    }

    /** @return true end of line reached */
    private fun collect(): Boolean {
        val endOfLine = byteBuf.findEndOfLine()
        val end = when {
            endOfLine == 0 -> return true
            endOfLine < 0 -> byteBuf.limit()
            else -> endOfLine
        }
        val limit = byteBuf.limit()
        byteBuf.limit(end)
        byteCount += lineBuilder.append(byteBuf).toULong()
        byteBuf.limit(limit)
        return endOfLine >= 0
    }

    private fun updateReading(loaded: ULong) {
        var loaded = loaded
        var length = length
        var denominator = 1
        while (length > Int.MAX_VALUE.toULong()) {
            length /= 2uL
            loaded /= 2uL
            denominator *= 2
        }
        reading.value = Reading(loaded.toInt(), length.toInt(), denominator)
    }

    private fun ByteBuffer.findEndOfLine(): Int {
        val array = array()
        for (i in position()..<limit() step byteStep) {
            val byte = array[i]
            when (byteStep) {
                1 if (byte == LF || byte == CR) -> Unit
                1 -> continue
                else -> when (array.getOrNull(i.inc())) {
                    NULL if byte == LF -> Unit
                    NULL if byte == CR -> Unit
                    LF if byte == NULL -> Unit
                    CR if byte == NULL -> Unit
                    else -> continue
                }
            }
            return i
        }
        return -1
    }
}
