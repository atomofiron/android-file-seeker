package app.atomofiron.searchboxapp.model.textviewer

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.TextSearchTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.lang.Exception

class TextViewerSession(node: Node) {
    val mutex = Mutex()
    val item = MutableStateFlow(node)
    val reader = File(node.ref.string).run {
        try { // todo lazy reading in Rust
            inputStream().reader().buffered()
        } catch (e: Exception) {
            // FileNotFoundException, ...
            null
        }
    }
    private val _lines = MutableStateFlow<List<TextLine>>(listOf())
    val lines: StateFlow<List<TextLine>> = _lines
    val loading = MutableStateFlow(false)
    private val _tasks = MutableStateFlow<List<TextSearchTask>>(listOf())
    val tasks: StateFlow<List<TextSearchTask>> = _tasks

    var isFullyRead: Boolean = false
        private set

    suspend fun tasks(action: MutableList<TextSearchTask>.() -> Unit) = mutex.withLock {
        _tasks.run {
            value = value.toMutableList().apply(action)
        }
    }

    suspend fun textLines(action: MutableList<TextLine>.() -> Boolean) = mutex.withLock {
        _lines.run {
            value = value.toMutableList().apply {
                isFullyRead = action()
            }
        }
    }
}
