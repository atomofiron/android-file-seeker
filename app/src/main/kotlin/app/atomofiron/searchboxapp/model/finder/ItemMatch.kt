package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.searchboxapp.model.explorer.NodeHash
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.textviewer.MutableMatchMap

sealed class ItemMatch(val ref: NodeRef) {

    abstract val meta: NodeHash
    abstract val count: Int

    val uniqueId get() = ref.uniqueId
    val withCounter: Boolean get() = this is Many

    data class One(
        override val meta: NodeHash,
    ) : ItemMatch(meta.ref) {
        override val count = 1
    }

    data class Many(
        override val meta: NodeHash,
        override val count: Int = 0,
        val matches: MutableMatchMap = hashMapOf(),
    ) : ItemMatch(meta.ref)
}
