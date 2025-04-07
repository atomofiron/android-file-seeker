package app.atomofiron.searchboxapp.injectable.interactor

import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.injectable.service.UtilService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey

class ExplorerInteractor(
    private val scope: CoroutineScope,
    private val service: ExplorerService,
    private val utils: UtilService,
) {
    private val context = Dispatchers.IO

    fun getFlow(tab: NodeTabKey) = service.getOrCreateFlowSync(tab)

    fun copyToClipboard(item: Node) = utils.copyToClipboard(item)

    fun selectRoot(tab: NodeTabKey, item: NodeRoot) {
        scope.launch(context) {
            service.trySelectRoot(tab, item)
        }
    }

    fun checkItem(tab: NodeTabKey, item: Node, isChecked: Boolean) {
        scope.launch(context) {
            service.tryCheckItem(tab, item, isChecked)
        }
    }

    fun toggleDir(tab: NodeTabKey, dir: Node) {
        scope.launch(context) {
            service.tryToggle(tab, dir)
        }
    }

    fun updateItems(tab: NodeTabKey, items: List<Node>) {
        scope.launch(context) {
            items.forEach {
                launch {
                    service.tryCache(tab, it)
                }
            }
        }
    }

    fun updateRoots(tab: NodeTabKey) {
        scope.launch(context) {
            service.updateRootsAsync(tab)
        }
    }

    fun deleteItems(tab: NodeTabKey, items: List<Node>) {
        scope.launch(context) {
            service.tryDelete(tab, items)
        }
    }

    fun rename(tab: NodeTabKey, item: Node, name: String) {
        scope.launch(context) {
            service.tryRename(tab, item, name)
        }
    }

    fun create(tab: NodeTabKey, dir: Node, name: String, directory: Boolean) {
        scope.launch(context) {
            service.tryCreate(tab, dir, name, directory)
        }
    }

    fun clone(tab: NodeTabKey, target: Node, name: String) {
        scope.launch(context) {
            var to = target.rename(name)
            if (to.isDirectory) to = to.copy(children = null)
            service.tryCopy(tab, target, to, asMoving = false)
        }
    }
}