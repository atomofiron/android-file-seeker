package app.atomofiron.searchboxapp.model.textviewer

import app.atomofiron.searchboxapp.model.explorer.Node
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.lang.Exception

class TextViewerSession(node: Node) {
    val mutex = Mutex()
    val item = MutableStateFlow(node)
    val reader = File(node.path).run {
        try {
            inputStream().reader().buffered()
        } catch (e: Exception) {
            // FileNotFoundException, ...
            null
        }
    }
    val textLines = MutableStateFlow<List<TextLine>>(listOf())
    val textLoading = MutableStateFlow(false)
    val tasks = MutableStateFlow<List<SearchTask>>(listOf())

    var isFullyRead: Boolean = false
}
