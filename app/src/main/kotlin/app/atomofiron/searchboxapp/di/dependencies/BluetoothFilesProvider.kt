package app.atomofiron.searchboxapp.di.dependencies

import androidx.annotation.RequiresApi
import app.atomofiron.common.util.Sdk
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.utils.CoroutineLauncher
import app.atomofiron.searchboxapp.utils.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BluetoothFilesProvider @RequiresApi(Sdk.Q) constructor(
    private val scope: CoroutineScope,
) : CoroutineLauncher by CoroutineLauncher(scope) {

    val files: StateFlow<List<Node>>
        field = MutableStateFlow<List<Node>>(emptyList())
    val updates: Flow<Pair<Node, Boolean>>
        field = MutableSharedFlow<Pair<Node, Boolean>>(extraBufferCapacity = Int.MAX_VALUE)

    fun update(items: List<Node>) {
        files.value = items
    }

    fun add(item: Node) {
        files.value = files.value.mutate {
            add(item)
        }
        default {
            updates.emit(item to true)
        }
    }

    fun remove(item: Node) {
        files.value = files.value.mutate {
            remove(item)
        }
        default {
            updates.emit(item to false)
        }
    }
}