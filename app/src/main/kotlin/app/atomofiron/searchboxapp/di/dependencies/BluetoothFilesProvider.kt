package app.atomofiron.searchboxapp.di.dependencies

import androidx.annotation.RequiresApi
import app.atomofiron.common.util.Sdk
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.utils.mutate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BluetoothFilesProvider @RequiresApi(Sdk.Q) constructor() {

    val files: StateFlow<List<Node>>
        field = MutableStateFlow<List<Node>>(emptyList())

    fun update(items: List<Node>) {
        files.value = items
    }

    fun add(item: Node) {
        files.value = files.value.mutate {
            add(item)
        }
    }

    fun remove(item: Node) {
        files.value = files.value.mutate {
            remove(item)
        }
    }
}