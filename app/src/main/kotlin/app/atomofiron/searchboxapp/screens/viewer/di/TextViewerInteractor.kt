package app.atomofiron.searchboxapp.screens.viewer.di

import app.atomofiron.searchboxapp.android.NativeBridge
import app.atomofiron.searchboxapp.di.dependencies.service.TextViewerService
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.QueryParams
import app.atomofiron.searchboxapp.model.finder.LocalSearchTask
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.viewer.TextViewerScope
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNode
import app.atomofiron.searchboxapp.utils.ExplorerUtils.update
import app.atomofiron.searchboxapp.utils.Rslt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@TextViewerScope
class TextViewerInteractor @Inject constructor(
    private val scope: CoroutineScope,
    private val service: TextViewerService,
    private val store: ExplorerStore,
    private val utils: UtilService,
    preferences: PreferenceStore,
) {
    private val context = Dispatchers.IO
    private val asSu by preferences.asSu

    fun fetchItem(ref: NodeRef): Node {
        store.currentItems
            .find { it.ref == ref }
            ?.let { return it }
        val item = ref.toNode()
        return item.update(asSu)
    }

    fun fetchFileSession(ref: NodeRef): Rslt<TextViewerSession> = service.getFileSession(ref)

    /** invoke the callback after success */
    fun readFileToLine(ref: NodeRef, index: Int, callback: (() -> Unit)? = null) {
        scope.launch(context) {
            service.readFile(ref, index) { success ->
                if (success) callback?.invoke()
            }
        }
    }

    suspend fun fetchTask(ref: NodeRef, taskId: UUID): LocalSearchTask? = service.fetchTask(ref, taskId)

    fun getHash(ref: NodeRef): Rslt<Int> = NativeBridge.crcHash(ref, asSu)

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

    fun copy(item: Node) = utils.copyToClipboard(item)
}