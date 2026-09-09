package app.atomofiron.searchboxapp.di.dependencies

import androidx.annotation.RequiresApi
import app.atomofiron.common.util.Sdk
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.utils.mutate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BluetoothFilesProvider @RequiresApi(Sdk.Q) constructor() {

    val files: StateFlow<List<NodeRef>>
        field = MutableStateFlow<List<NodeRef>>(emptyList())

    fun update(items: List<NodeRef>) {
        files.value = items
    }

    fun add(item: NodeRef) {
        files.value = files.value.mutate {
            add(item)
        }
    }

    fun remove(item: NodeRef) {
        files.value = files.value.mutate {
            remove(item)
        }
    }
}