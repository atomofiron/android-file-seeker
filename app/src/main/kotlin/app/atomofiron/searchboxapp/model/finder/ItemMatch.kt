package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.textviewer.MutableMatchMap

sealed interface ItemMatch {

    val item: Node
    val count: Int

    val path get() = item.ref
    val isDirectory: Boolean get() = item.isDirectory
    val isCached: Boolean get() = item.isCached
    val isDeleting: Boolean get() = item.state.isDeleting
    val isChecked: Boolean get() = item.isChecked
    val withCounter: Boolean get() = this is Many

    fun update(item: Node): ItemMatch

    data class Single(override val item: Node) : ItemMatch {
        override val count = 1
        override fun update(item: Node): Single = copy(item = item)
    }

    data class Many(
        override val item: Node,
        override val count: Int = 0,
        val matches: MutableMatchMap = hashMapOf(),
    ) : ItemMatch {
        override fun update(item: Node): Many = copy(item = item)
    }
}
