package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.searchboxapp.model.explorer.NodeHash
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.textviewer.MutableMatchMap
import kotlinx.serialization.Serializable

@Serializable
sealed class ItemMatch(val ref: NodeRef) {

    abstract val hash: NodeHash
    abstract val count: Int

    val uniqueId get() = ref.uniqueId
    val withCounter: Boolean get() = this is Many

    @Serializable
    data class One(
        override val hash: NodeHash,
    ) : ItemMatch(hash.ref) {
        override val count = 1
    }

    @Serializable
    data class Many(
        override val hash: NodeHash,
        override val count: Int = 0,
        val matches: MutableMatchMap = hashMapOf(),
    ) : ItemMatch(hash.ref)
}
