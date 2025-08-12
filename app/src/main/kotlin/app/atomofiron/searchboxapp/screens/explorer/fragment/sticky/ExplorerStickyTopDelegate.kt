package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky

import android.view.View
import android.view.View.MeasureSpec
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.CoroutineListDiffer
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.common.util.noClip
import app.atomofiron.searchboxapp.custom.view.ExplorerStickyTopView
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl.ExplorerItemBinderActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isDot
import kotlin.math.max

class ExplorerStickyTopDelegate(
    private val recyclerView: RecyclerView,
    private val stickyBox: FrameLayout,
    private val roots: RootAdapter,
    private val adapter: ExplorerAdapter,
    private var listener: ExplorerItemBinderActionListener,
) : RecyclerView.OnScrollListener(), RecyclerView.OnChildAttachStateChangeListener, CoroutineListDiffer.ListListener<Node> {

    private val context = recyclerView.context
    private val threshold get() = stickyBox.paddingTop
    private var composition: ExplorerItemComposition? = null
    private val holders = HashMap<Int, HolderData>()
    private val stickies = HashMap<Int, StickyData>()

    init {
        recyclerView.addOnScrollListener(this)
        recyclerView.addOnChildAttachStateChangeListener(this)
        adapter.addListListener(this)
        stickyBox.noClip()
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy != 0) updateOffset()
    }

    override fun onCurrentListChanged(current: List<Node>) {
        val opened = mutableListOf<Node>()
        for (i in current.indices) {
            val new = current[i]
            syncStickies(new, i)
            syncHolders(new, i)
            if (new.isOpened) opened.add(new)
        }
        for (sticky in stickies.entries.toList()) {
            if (!opened.any { it.uniqueId == sticky.value.item.uniqueId }) {
                removeSticky(sticky.key)
            }
        }
        recyclerView.doOnPreDraw { updateOffset() }
    }

    override fun onChanged(index: Int, new: Node) {
        syncStickies(new, index)
        syncHolders(new, index)
        updateOffset()
    }

    override fun onChildViewAttachedToWindow(itemView: View) {
        val holder = itemView.getHolder()
        val item = holder?.let { adapter.items[it.bindingAdapterPosition] }
        item ?: return
        holders[item.uniqueId] = HolderData(item, holder)
    }

    override fun onChildViewDetachedFromWindow(itemView: View) {
        holders.entries
            .find { it.value.holder.itemView === itemView }
            ?.key
            ?.let { holders.remove(it) }
    }

    private fun View.getHolder(): RecyclerView.ViewHolder? {
        return recyclerView.getChildViewHolder(this)
            .takeIf { it.absoluteAdapterPosition >= roots.itemCount }
    }

    fun onDecoratorDraw() = updateOffset()

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        for (sticky in stickies.values) {
            sticky.view.bind(composition = composition)
        }
    }

    private fun syncHolders(new: Node, position: Int) {
        val holder = holders[new.uniqueId]
        if (holder?.item?.areContentsTheSame(new) == false) {
            holders[new.uniqueId] = HolderData(new, holder.holder)
        } else if (holder != null) {
            debugRequire(position == holder.position)
        }
    }

    private fun syncStickies(new: Node, position: Int) {
        if (!new.isOpened || new.isEmpty) {
            removeSticky(new.uniqueId)
            return
        }
        if (new.isDot()) {
            return
        }
        val sticky = stickies[new.uniqueId]
        if (sticky?.item?.areContentsTheSame(new) != true) {
            val view = sticky?.view
                ?.takeIf { sticky.item.isDeepest == new.isDeepest }
                ?: removeSticky(new.uniqueId)
                    .let { newSticky(new) }
            view.bind(new)
            stickies[new.uniqueId] = StickyData(position, new, view)
        }
    }

    private fun removeSticky(uniqueId: Int) {
        stickies.remove(uniqueId)?.let {
            stickyBox.removeView(it.view)
        }
    }

    private fun newSticky(new: Node): ExplorerStickyTopView {
        val view = ExplorerStickyTopView(context, new.isDeepest, listener)
        view.bind(new, composition)
        view.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        stickyBox.addView(view)
        stickyBox.takeIf { it.measuredWidth > 0 }?.let {
            view.measure(
                MeasureSpec.makeMeasureSpec(it.measuredWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            )
        }
        return view
    }

    private fun updateOffset() {
        val holders = holders.values.sortedBy { it.position }
        val stickies = stickies.values.sortedBy { -it.position }
        val last = holders.lastOrNull() ?: return
        for (sticky in stickies) {
            val holder = holders.takeIf { sticky.position <= last.position }
                ?.findHolder(sticky)
                ?.takeIf { it.holder.itemView.top < threshold }
                .also { sticky.view.isVisible = it != null }
                ?: continue
            require(sticky.view.measuredHeight > 0)
            var top = holder.holder.itemView.top
            top = max(top, threshold)
            holders.findBarrier(sticky)?.let { barrier ->
                val bottom = top + sticky.view.measuredHeight
                top -= max(0, bottom - barrier)
            }
            sticky.view.move(top, drawTop = (top - holder.holder.itemView.top).toFloat())
        }
    }

    /** @return some holder to move sticky with, the same opened dir or some child above the next opened */
    private fun List<HolderData>.findHolder(sticky: StickyData): HolderData? {
        find { it.position == sticky.position }
            ?.let { return it }
        val openedIndex = sticky.item.getOpenedIndex(sticky.item.childCount)
        for (holder in this) {
            return sticky.item.children
                .takeIf { !holder.item.isDot() && holder.item.parentPath == sticky.item.path }
                ?.indexOfFirst { it.uniqueId == holder.item.uniqueId }
                ?.takeIf { it < openedIndex }
                ?.let { return holder }
        }
        return null
    }

    /** @return the top of the next opened dir or the bottom of the last child */
    private fun List<HolderData>.findBarrier(sticky: StickyData): Int? {
        for (i in indices) {
            val data = get(i)
            return when {
                // skip above the target
                data.position <= sticky.position -> continue
                // next opened dir below
                !sticky.item.isDeepest && data.item.isOpened -> data.holder.itemView.top
                // skip children
                data.item.parentPath == sticky.item.path -> continue
                // nothing above
                i == 0 -> return null
                // the last child
                else -> get(i.dec()).holder.itemView.bottom
            }
        }
        return null
    }
}