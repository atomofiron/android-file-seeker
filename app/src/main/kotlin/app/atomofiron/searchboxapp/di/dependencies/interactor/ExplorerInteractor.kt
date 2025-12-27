package app.atomofiron.searchboxapp.di.dependencies.interactor

import app.atomofiron.common.util.extension.launchOnDefault
import app.atomofiron.searchboxapp.di.dependencies.service.ExplorerService
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.ExplorerTabKey
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.screens.explorer.ExplorerScope
import app.atomofiron.searchboxapp.utils.ExplorerUtils.move
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

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

@ExplorerScope
class ExplorerInteractor @Inject constructor(
    private val scope: CoroutineScope,
    private val service: ExplorerService,
    private val store: ExplorerStore,
    private val utils: UtilService,
) {
    private val context = Dispatchers.IO

    fun getFlow(key: ExplorerTabKey) = service.getFlow(key)

    fun drop(vararg keys: ExplorerTabKey) = service.drop(*keys)

    fun copyToClipboard(item: Node) = utils.copyToClipboard(item, withAlert = false)

    fun toggleRoot(key: ExplorerTabKey, item: NodeRoot) {
        scope.launch(context) {
            service.tryToggleRoot(key, item)
        }
    }

    fun check(tab: ExplorerTabKey, item: Node, toChecked: Boolean) = check(tab, listOf(item), toChecked)

    fun check(tab: ExplorerTabKey, items: List<Node>, toChecked: Boolean) {
        scope.launch(context) {
            service.tryCheck(tab, items, toChecked)
        }
    }

    fun toggleDir(key: ExplorerTabKey, dir: Node) {
        scope.launch(context) {
            service.tryToggle(key, dir)
        }
    }

    fun updateItems(key: ExplorerTabKey, items: List<Node>) {
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
            service.updateRootsAsync()
        }
    }

    fun setCurrentTab(key: ExplorerTabKey) {
        scope.launchOnDefault {
            store.setCurrentTab(key)
        }
    }

    fun deleteItems(key: ExplorerTabKey, items: List<Node>) {
        scope.launch(context) {
            service.tryDelete(key, items)
        }
    }

    fun rename(key: ExplorerTabKey, ref: NodeRef, name: String) {
        scope.launch(context) {
            service.tryRename(key, ref, name)
        }
    }

    fun create(key: ExplorerTabKey, parent: Node, name: String, directory: Boolean) {
        scope.launch(context) {
            service.tryCreate(key, parent, name, directory)
        }
    }

    fun clone(key: ExplorerTabKey, target: Node, name: String) {
        scope.launch(context) {
            var to = target.move(name = name)
            if (to.isDirectory) to = to.copy(children = null)
            service.tryCopy(key, target, to, asMoving = false)
        }
    }

    fun resetChecked(key: ExplorerTabKey) {
        scope.launch(context) {
            service.resetChecked(key)
        }
    }
}