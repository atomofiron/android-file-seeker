package app.atomofiron.searchboxapp.di.dependencies.interactor

import app.atomofiron.searchboxapp.di.dependencies.service.ExplorerService
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.utils.ExplorerUtils.move
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class Cell<T> {
    open val value: T get() = throw Exception()
    var next: Cell<T>? = null

    class Link<T> : Cell<T>()
    class Data<T>(override val value: T) : Cell<T>()
}

class Chain<T : Any> {
    private var chain: Cell<T> = Cell.Link()

    fun push(value: T) {
        val link = Cell.Link<T>()
        val data = Cell.Data(value)
        link.next = data
        data.next = chain
        chain = link
    }

    fun pull(value: T): T? {
        var x: Cell<T>? = null
        var y: Cell<T>? = null
        var z: Cell<T> = chain
        if (z.next == null) {
            return null
        }
        while (z.next != null) {
            x = z
            y = z.next
            z = y!!.next!!
        }
        x!!.next = null
        return y!!.value
    }
}

class ExplorerInteractor(
    private val scope: CoroutineScope,
    private val service: ExplorerService,
    private val store: ExplorerStore,
    private val utils: UtilService,
) {
    private val context = Dispatchers.IO

    fun getFlow(key: NodeTabKey) = service.getFlow(key)

    fun copyToClipboard(item: Node) = utils.copyToClipboard(item)

    fun toggleRoot(key: NodeTabKey, item: NodeRoot) {
        scope.launch(context) {
            service.tryToggleRoot(key, item)
        }
    }

    fun checkItem(tab: NodeTabKey, item: Node, isChecked: Boolean) {
        scope.launch(context) {
            service.tryCheckItem(tab, item, isChecked)
        }
    }

    fun toggleDir(key: NodeTabKey, dir: Node) {
        scope.launch(context) {
            service.tryToggle(key, dir)
        }
    }

    fun updateItems(key: NodeTabKey, items: List<Node>) {
        scope.launch(context) {
            items.forEach {
                launch {
                    service.tryCache(key, it)
                }
            }
        }
    }

    fun updateRoots() {
        scope.launch(context) {
            service.updateRootsAsync(store.storage.value)
        }
    }

    fun updateCurrentTab(key: NodeTabKey) {
        scope.launch(Dispatchers.Default) {
            store.setCurrentTab(key)
        }
    }

    fun deleteItems(key: NodeTabKey, items: List<Node>) {
        scope.launch(context) {
            service.tryDelete(key, items)
        }
    }

    fun rename(key: NodeTabKey, item: Node, name: String) {
        scope.launch(context) {
            service.tryRename(key, item, name)
        }
    }

    fun create(key: NodeTabKey, dir: Node, name: String, directory: Boolean) {
        scope.launch(context) {
            service.tryCreate(key, dir, name, directory)
        }
    }

    fun clone(key: NodeTabKey, target: Node, name: String) {
        scope.launch(context) {
            var to = target.move(name = name)
            if (to.isDirectory) to = to.copy(children = null)
            service.tryCopy(key, target, to, asMoving = false)
        }
    }

    fun resetChecked(key: NodeTabKey) {
        scope.launch(context) {
            service.resetChecked(key)
        }
    }
}