package app.atomofiron.searchboxapp.model.textviewer

import app.atomofiron.common.util.GrowingList
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.TextSearchTask
import app.atomofiron.searchboxapp.utils.ByteArrayBuilder
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset

private const val BUFFER_SIZE = 8 * 1024
private const val CR: Byte = 0x0D
private const val LF: Byte = 0x0A

class TextViewerSession private constructor(
    val input: FileInputStream,
    ref: NodeRef,
) : Closeable {
    companion object {
        operator fun invoke(ref: NodeRef): TextViewerSession? = try {
            val input = FileInputStream(File(ref.string))
            TextViewerSession(input, ref)
        } catch (_: Exception) { // FileNotFoundException, ...
            null
        }
    }

    private var bytes = ByteArray(BUFFER_SIZE)
    private var byteBuf = ByteBuffer.wrap(bytes)
    private val lineBuilder = ByteArrayBuilder()
    private var byteCount = 0
    var isFullyRead = false
        private set

    val mutex = Mutex()
    private val _item = MutableStateFlow(ref.toNode())
    val item: StateFlow<Node> = _item
    private val lineList = GrowingList<TextLine>()
    private val _lines = MutableStateFlow<List<TextLine>>(listOf())
    val lines: StateFlow<List<TextLine>> = _lines
    val loading = MutableStateFlow(false)
    private val _tasks = MutableStateFlow<List<TextSearchTask>>(listOf())
    val tasks: StateFlow<List<TextSearchTask>> = _tasks

    init {
        Charset.availableCharsets().keys.forEach { println(it) }
        byteBuf.limit(0)
    }

    fun updateItem(item: Node) {
        _item.value = item
    }

    suspend fun tasks(action: suspend MutableList<TextSearchTask>.() -> Unit) = mutex.withLock {
        _tasks.run {
            value = value.toMutableList()
                .apply { action() }
        }
    }

    suspend fun textLines(action: suspend GrowingList<TextLine>.() -> Unit) = mutex.withLock {
        _lines.run {
            lineList.action()
            value = lineList.fetch()
        }
    }

    override fun close() = input.close()

    fun readLine(): TextLine? {
        var offset = byteCount
        if (isFullyRead) {
            return null
        }
        while (true) {
            offset += skipCrLf()
            when {
                isFullyRead -> break
                !byteBuf.hasRemaining() -> isFullyRead = fillBuffer()
                collect() -> break
            }
        }
        val text = lineBuilder.toByteArray()
        lineBuilder.clear()
        return TextLine(offset, text)
    }

    /** @return true if EOF */
    private fun fillBuffer(): Boolean {
        byteBuf.clear()
        val read = input.read(byteBuf.array(), 0, byteBuf.capacity())
        return (read < 0).also {
            when {
                it -> byteBuf.limit(0)
                else -> byteBuf.limit(read)
            }
        }
    }

    /** @return count of skipped bytes */
    private fun skipCrLf(): Int {
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
            byteBuf.remaining() == 1 -> 1
            afterNext == LF || afterNext == CR -> 2
            else -> 1
        }
        byteCount += skip
        byteBuf.position(byteBuf.position() + skip)
        return skip
    }

    /** @return true end of line reached */
    private fun collect(): Boolean {
        val limit = byteBuf.limit()
        val endOfLine = byteBuf.findEndOfLine()
        val end = if (endOfLine < 0) byteBuf.limit() else endOfLine
        byteBuf.limit(end)
        byteCount += lineBuilder.append(byteBuf)
        byteBuf.limit(limit)
        return endOfLine >= 0
    }

    private fun ByteBuffer.findEndOfLine(): Int {
        val array = array()
        for (i in position()..<limit()) {
            if (array[i] == LF || array[i] == CR) {
                return i
            }
        }
        return -1
    }
}
