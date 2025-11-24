package app.atomofiron.searchboxapp.di.dependencies.interactor

import app.atomofiron.searchboxapp.di.dependencies.service.TextViewerService
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.QueryParams
import app.atomofiron.searchboxapp.model.finder.TextSearchTask
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNode
import app.atomofiron.searchboxapp.utils.ExplorerUtils.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class TextViewerInteractor(
    private val scope: CoroutineScope,
    private val service: TextViewerService,
    private val store: ExplorerStore,
    preferenceStore: PreferenceStore,
) {
    private val context = Dispatchers.IO
    private val asSu by preferenceStore.asSu

    fun fetchItem(ref: NodeRef): Node {
        store.currentItems
            .find { it.ref == ref }
            ?.let { return it }
        val item = ref.toNode()
        return item.update(asSu)
    }

    fun fetchFileSession(ref: NodeRef): TextViewerSession? = service.getFileSession(ref)

    /** invoke the callback after success */
    fun readFileToLine(ref: NodeRef, index: Int, callback: (() -> Unit)? = null) {
        scope.launch(context) {
            service.readFile(ref, index) { success ->
                if (success) callback?.invoke()
            }
        }
    }

    fun fetchTask(ref: NodeRef, taskId: UUID, callback: (TextSearchTask) -> Unit) {
        scope.launch {
            val task = service.fetchTask(ref, taskId)
            task?.let(callback)
        }
    }

    fun search(ref: NodeRef, params: QueryParams) {
        scope.launch(Dispatchers.IO) {
            service.search(ref, params)
        }
    }

    fun removeTask(ref: NodeRef, taskId: Int) {
        scope.launch {
            service.removeTask(ref, taskId)
        }
    }

    fun closeSession(ref: NodeRef) = service.closeSession(ref)
}