package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky

import android.view.View
import android.widget.FrameLayout
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.CoroutineListDiffer
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl.ExplorerItemBinderActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.info.HolderInfo

class HolderDelegate(
    private val recyclerView: RecyclerView,
    stickyBox: FrameLayout,
    private val roots: RootAdapter,
    private val adapter: ExplorerAdapter,
    listener: ExplorerItemBinderActionListener,
) : RecyclerView.OnChildAttachStateChangeListener, CoroutineListDiffer.ListListener<Node>, View.OnLayoutChangeListener {

    private val holders = HashMap<Int, HolderInfo>()
    private val top = StickyTopDelegate(holders.values, stickyBox, listener)
    private val bottom = StickyBottomDelegate(holders.values, stickyBox, listener)

    init {
        recyclerView.addOnChildAttachStateChangeListener(this)
        recyclerView.addOnLayoutChangeListener(this)
        adapter.addListListener(this)
    }

    fun setComposition(composition: ExplorerItemComposition) = top.setComposition(composition)

    override fun onChildViewAttachedToWindow(itemView: View) {
        val holder = itemView.getHolder()
        val item = holder?.let { adapter.items[it.bindingAdapterPosition] }
        item ?: return
        val info = HolderInfo(holder.bindingAdapterPosition, item, holder)
        holders[item.uniqueId] = info
        bottom.onAttach(info)
    }

    override fun onChildViewDetachedFromWindow(itemView: View) {
        holders.entries
            .find { it.value.view === itemView }
            ?.key
            ?.let { holders.remove(it) }
            ?.let { bottom.onDetach(it) }
    }

    override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        updateOffset()
    }

    override fun onCurrentListChanged(current: List<Node>) {
        val opened = mutableListOf<Pair<Int,Node>>()
        val separators = mutableListOf<Pair<Int,Node>>()
        for (i in current.indices) {
            val new = current[i]
            syncHolders(new, i)
            when {
                top.valid(new) -> opened.add(i to new)
                bottom.valid(new) -> separators.add(i to new)
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
        if (top.valid(new)) {
            top.sync(new, index)
        }
    }

    private fun syncHolders(new: Node, position: Int) {
        val holder = holders[new.uniqueId] ?: return
        if (holder.position != position || !holder.item.areContentsTheSame(new)) {
            holders[new.uniqueId] = HolderInfo(position, new, holder.holder)
        }
    }

    private fun View.getHolder(): RecyclerView.ViewHolder? {
        return recyclerView.getChildViewHolder(this)
            .takeIf { it.absoluteAdapterPosition >= roots.itemCount }
    }

    private fun updateOffset() {
        top.updateOffset()
        bottom.updateOffset()
    }
}