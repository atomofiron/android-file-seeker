package app.atomofiron.searchboxapp.injectable.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.service.TextViewerService
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.SearchParams
import app.atomofiron.searchboxapp.model.textviewer.SearchTask
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import java.util.UUID

class TextViewerInteractor(
    private val scope: CoroutineScope,
    private val textViewerService: TextViewerService,
) {
    private val context = Dispatchers.IO

    fun fetchFileSession(path: String): TextViewerSession = textViewerService.getFileSession(path)

    /** invoke the callback after success */
    fun readFileToLine(item: Node, index: Int, callback: (() -> Unit)? = null) {
        scope.launch(context) {
            textViewerService.readFile(item, index) { success ->
                if (success) callback?.invoke()
            }
        }
    }

    fun fetchTask(item: Node, taskId: UUID, callback: (SearchTask) -> Unit) {
        scope.launch {
            val task = textViewerService.fetchTask(item, taskId)
            task?.let(callback)
        }
    }

    fun search(item: Node, params: SearchParams) {
        scope.launch(Dispatchers.IO) {
            textViewerService.search(item, params)
        }
    }

    fun removeTask(item: Node, taskId: Int) {
        scope.launch {
            textViewerService.removeTask(item, taskId)
        }
    }

    fun closeSession(item: Node) = textViewerService.closeSession(item)
}