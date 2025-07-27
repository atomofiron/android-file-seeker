package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NodeGarden {

    val states = mutableListOf<NodeState>()
    val mutex = Mutex()
    val trees = HashMap<NodeTabKey, NodeTab>()

    suspend inline fun <R> withGarden(action: NodeGarden.() -> R): R {
        return mutex.withLock {
            action()
        }
    }

    operator fun get(key: NodeTabKey): NodeTab? = trees[key]
}