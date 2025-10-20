package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NodeGarden {

    val roots = mutableListOf<NodeRoot>()
    val states = mutableListOf<NodeStateImpl>()
    val mutex = Mutex()
    val tabs = mutableMapOf<NodeTabKey, NodeTab>()

    fun has(key: NodeTabKey): Boolean = tabs.containsKey(key)

    operator fun get(key: NodeTabKey): NodeTab = tabs.getOrPut(key) {
        if (key is NodeTabKey.Explorer && key.pickerTypes != null) {
            tabs[NodeTabKey.Explorer(key.index, null)]
                ?.let { return@getOrPut it.clone(key, key.pickerTypes) }
        }
        NodeTab(key, roots, states)
    }

    operator fun get(item: Node): NodeStateImpl? = states.find { it.uniqueId == item.uniqueId }

    fun getFlow(key: NodeTabKey): StateFlow<NodeTabItems> = get(key).flow

    fun drop(vararg keys: NodeTabKey) = keys.forEach {
        tabs.remove(it)
    }

    suspend inline operator fun <R> invoke(action: NodeGarden.() -> R): R {
        return mutex.withLock { action() }
    }

    suspend inline operator fun <R> invoke(key: NodeTabKey, action: NodeTab.() -> R): R? {
        return mutex.withLock { get(key).action() }
    }
}