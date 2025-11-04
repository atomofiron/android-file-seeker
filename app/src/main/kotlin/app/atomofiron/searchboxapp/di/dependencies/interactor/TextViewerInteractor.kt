package app.atomofiron.searchboxapp.di.dependencies.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.di.dependencies.service.TextViewerService
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.QueryParams
import app.atomofiron.searchboxapp.model.finder.TextSearchTask
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import java.util.UUID

class TextViewerInteractor(
    private val scope: CoroutineScope,
    private val textViewerService: TextViewerService,
) {
    private val context = Dispatchers.IO

    fun fetchFileSession(ref: NodeRef): TextViewerSession = textViewerService.getFileSession(ref)

    /** invoke the callback after success */
    fun readFileToLine(ref: NodeRef, index: Int, callback: (() -> Unit)? = null) {
        scope.launch(context) {
            textViewerService.readFile(ref, index) { success ->
                if (success) callback?.invoke()
            }
        }
    }

    fun fetchTask(ref: NodeRef, taskId: UUID, callback: (TextSearchTask) -> Unit) {
        scope.launch {
            val task = textViewerService.fetchTask(ref, taskId)
            task?.let(callback)
        }
    }

    fun search(ref: NodeRef, params: QueryParams) {
        scope.launch(Dispatchers.IO) {
            textViewerService.search(ref, params)
        }
    }

    fun removeTask(ref: NodeRef, taskId: Int) {
        scope.launch {
            textViewerService.removeTask(ref, taskId)
        }
    }

    fun closeSession(ref: NodeRef) = textViewerService.closeSession(ref)
}