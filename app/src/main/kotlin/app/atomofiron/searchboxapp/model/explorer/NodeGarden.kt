package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.collections.HashMap

class NodeGarden {

    val states = LinkedList<NodeState>()
    val mutex = Mutex()
    val trees = HashMap<NodeTabKey, NodeTabTree>()

    suspend inline fun <R> withGarden(action: NodeGarden.() -> R): R {
        return mutex.withLock {
            action()
        }
    }

    operator fun get(key: NodeTabKey): NodeTabTree? = trees[key]
}