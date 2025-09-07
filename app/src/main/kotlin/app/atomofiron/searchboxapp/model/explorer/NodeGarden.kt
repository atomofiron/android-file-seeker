package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NodeGarden(vararg keys: NodeTabKey) {

    val roots = mutableListOf<NodeRoot>()
    val states = mutableListOf<NodeState>()
    val mutex = Mutex()
    val tabs = keys.associateWith { NodeTab(it, roots, states) }

    operator fun get(key: NodeTabKey): NodeTab = tabs[key]!!

    operator fun get(item: Node): NodeState? = states.find { it.uniqueId == item.uniqueId }

    fun getFlow(key: NodeTabKey): StateFlow<NodeTabItems> = tabs[key]!!.flow

    suspend inline operator fun <R> invoke(action: NodeGarden.() -> R): R {
        return mutex.withLock { action() }
    }

    suspend inline operator fun <R> invoke(key: NodeTabKey, action: NodeTab.() -> R): R? {
        return mutex.withLock { get(key).action() }
    }
}