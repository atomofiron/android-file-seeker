package app.atomofiron.searchboxapp.injectable.interactor

import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeState
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey

class ExplorerInteractor(
    private val scope: CoroutineScope,
    private val service: ExplorerService,
) {
    private val context = Dispatchers.IO

    fun getFlow(tab: NodeTabKey) = service.getOrCreateFlowSync(tab)

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

    fun updateItem(tab: NodeTabKey, file: Node) {
        scope.launch(context) {
            service.tryCacheAsync(tab, file)
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