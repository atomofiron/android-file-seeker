package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky

import android.view.View
import android.widget.FrameLayout
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.CoroutineListDiffer
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl.ExplorerItemBinderActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.info.HolderInfo
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isSeparator

class HolderDelegate(
    private val recyclerView: RecyclerView,
    stickyBox: FrameLayout,
    private val roots: RootAdapter,
    private val adapter: ExplorerAdapter,
    listener: ExplorerItemBinderActionListener,
) : RecyclerView.OnChildAttachStateChangeListener, CoroutineListDiffer.ListListener<Node> {

    private val holders = HashMap<Int, HolderInfo>()
    private val top = StickyTopDelegate(holders.entries, stickyBox, listener)
    private val bottom = StickyBottomDelegate(holders.entries, stickyBox, listener)

    init {
        recyclerView.addOnChildAttachStateChangeListener(this)
        adapter.addListListener(this)
    }

    fun setComposition(composition: ExplorerItemComposition) = top.setComposition(composition)

    fun updateOffset() {
        top.updateOffset()
        bottom.updateOffset()
    }

    override fun onChildViewAttachedToWindow(itemView: View) {
        val holder = itemView.getHolder()
        val item = holder?.let { adapter.items[it.bindingAdapterPosition] }
        item ?: return
        holders[item.uniqueId] = HolderInfo(item, holder)
    }

    override fun onChildViewDetachedFromWindow(itemView: View) {
        holders.entries
            .find { it.value.holder.itemView === itemView }
            ?.key
            ?.let { holders.remove(it) }
    }

    override fun onCurrentListChanged(current: List<Node>) {
        val opened = mutableListOf<Pair<Int,Node>>()
        val separators = mutableListOf<Pair<Int,Node>>()
        for (i in current.indices) {
            val new = current[i]
            syncHolders(new, i)
            when {
                new.isSeparator() -> separators.add(i to new)
                new.isOpened && new.isEmpty == false -> opened.add(i to new)
            }
        }
        top.sync(opened)
        bottom.sync(separators)
        recyclerView.doOnPreDraw {
            top.updateOffset()
            bottom.updateOffset()
        }
    }

    override fun onChanged(index: Int, new: Node) {
        syncHolders(new, index)
        top.sync(new, index)
    }

    private fun syncHolders(new: Node, position: Int) {
        val holder = holders[new.uniqueId]
        if (holder?.item?.areContentsTheSame(new) == false) {
            holders[new.uniqueId] = HolderInfo(new, holder.holder)
        } else if (holder != null) {
            debugRequire(position == holder.position)
        }
    }

    private fun View.getHolder(): RecyclerView.ViewHolder? {
        return recyclerView.getChildViewHolder(this)
            .takeIf { it.absoluteAdapterPosition >= roots.itemCount }
    }
}