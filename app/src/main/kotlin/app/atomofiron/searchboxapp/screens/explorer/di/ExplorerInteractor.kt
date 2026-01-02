package app.atomofiron.searchboxapp.screens.explorer.di

import androidx.work.WorkManager
import app.atomofiron.common.util.extension.unit
import app.atomofiron.searchboxapp.di.dependencies.router.startReceiveInto
import app.atomofiron.searchboxapp.di.dependencies.service.ExplorerService
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.ExplorerTabKey
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.explorer.ExplorerScope
import app.atomofiron.searchboxapp.utils.CoroutineLauncher
import app.atomofiron.searchboxapp.utils.ExplorerUtils.move
import kotlinx.coroutines.CoroutineScope
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
    private val workManager: WorkManager,
) : CoroutineLauncher by CoroutineLauncher(scope) {

    fun getFlow(key: ExplorerTabKey) = service.getFlow(key)

    fun drop(vararg keys: ExplorerTabKey) = service.drop(*keys)

    fun toggleRoot(key: ExplorerTabKey, item: NodeRoot) = io {
        service.tryToggleRoot(key, item)
    }.unit()

    fun check(tab: ExplorerTabKey, item: Node, toChecked: Boolean) = check(tab, listOf(item), toChecked)

    fun check(tab: ExplorerTabKey, items: List<Node>, toChecked: Boolean) = io {
        service.tryCheck(tab, items, toChecked)
    }.unit()

    fun toggleDir(key: ExplorerTabKey, dir: Node) = io {
        service.tryToggle(key, dir)
    }.unit()

    fun updateItems(key: ExplorerTabKey, items: List<Node>) = io {
        items.forEach {
            launch {
                service.tryCache(key, it)
            }
        }
    }.unit()

    fun updateRoots() = io {
        service.updateRootsAsync()
    }.unit()

    fun setCurrentTab(key: ExplorerTabKey) = default {
        store.setCurrentTab(key)
    }.unit()

    fun copy(key: ExplorerTabKey, targets: List<Node>, dst: Node, move: Boolean) = default {
        service.tryCopy(key, targets, dst, move)
    }.unit()

    fun deleteItems(key: ExplorerTabKey, items: List<Node>) = io {
        service.tryDelete(key, items)
    }.unit()

    fun rename(key: ExplorerTabKey, ref: NodeRef, name: String) = io {
        service.tryRename(key, ref, name)
    }.unit()

    fun create(key: ExplorerTabKey, parent: Node, name: String, directory: Boolean) = io {
        service.tryCreate(key, parent, name, directory)
    }.unit()

    fun clone(key: ExplorerTabKey, target: Node, name: String) = io {
        var to = target.move(name = name)
        if (to.isDirectory) to = to.copy(children = null)
        service.tryCopy(key, target, to, withMoving = false)
    }.unit()

    fun resetChecked(key: ExplorerTabKey) = io {
        service.resetChecked(key)
    }.unit()

    fun startReceive(destination: NodeRef, mode: ActivityMode.Receive) {
        default {
            workManager.startReceiveInto(destination, mode)
        }
    }
}