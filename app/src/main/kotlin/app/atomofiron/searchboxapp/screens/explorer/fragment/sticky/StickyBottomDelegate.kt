package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky

import android.view.Gravity
import android.view.View.MeasureSpec
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import androidx.core.view.isVisible
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.ExplorerStickyBottomView
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinder.ExplorerItemBinderActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.info.HolderInfo
import app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.info.StickyInfo
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isSeparator
import kotlin.math.min

private typealias StickyBottom = StickyInfo<ExplorerStickyBottomView>

class StickyBottomDelegate(
    private val holders: Collection<HolderInfo>,
    private val stickyBox: FrameLayout,
    private var listener: ExplorerItemBinderActionListener,
) {
    private val stickies = HashMap<Int, StickyBottom>()
    private val wrapContent = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
    private val onClick: (Node) -> Unit = listener::onItemClick
    private val threshold get() = stickyBox.run { height - paddingBottom }
    private val space = stickyBox.resources.getDimensionPixelSize(R.dimen.padding_nano)

    fun valid(item: Node) = item.isSeparator()

    fun sync(separators: List<Pair<Int,Node>>, items: List<Node>) {
        for (sticky in stickies.entries.toList()) {
            if (separators.none { it.second.uniqueId == sticky.value.uniqueId }) {
                removeSticky(sticky.key)
            }
        }
        for ((position, item) in separators) {
            sync(item, position, items, force = true)
        }
    }

    fun onAttach(info: HolderInfo) {
        if (info.isSeparator) {
            updateOffset()
        }
    }

    fun onDetach(info: HolderInfo) {
        if (info.isSeparator) {
            info.view.translationX = 0f
            updateOffset()
        }
    }

    fun sync(new: Node, position: Int, items: List<Node>, force: Boolean = false) {
        debugRequire(new.isSeparator()) { "sync ${new.ref}" }
        val sticky = stickies[new.uniqueId]
        val view = when {
            sticky == null -> newSticky(new)
            sticky.position != position -> sticky.view
            !sticky.areContentsTheSame(new) -> sticky.view
            force -> sticky.view
            else -> return
        }
        val sorted = items.takeChildrenOf(new)
        stickies[new.uniqueId] = StickyInfo(position, new, view, sorted)
    }

    private fun removeSticky(uniqueId: Int) {
        stickies.remove(uniqueId)?.let {
            stickyBox.removeView(it.view)
        }
    }

    private fun newSticky(new: Node): ExplorerStickyBottomView {
        val view = ExplorerStickyBottomView(stickyBox.context, onClick)
        view.bind(new)
        view.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            .also { it.gravity = Gravity.BOTTOM }
        stickyBox.addView(view)
        stickyBox.takeIf { it.measuredWidth > 0 }?.let {
            view.measure(wrapContent, wrapContent)
        }
        return view
    }

    fun updateOffset() {
        val holders = holders.sortedBy { -it.position }
        val stickies = stickies.values.sortedBy { it.position }
        val first = holders.lastOrNull() ?: return
        for (sticky in stickies) {
            val holderBottom = holders
                .takeIf { sticky.position >= first.position }
                ?.takeIf { !it.moveOriginal(sticky.position) }
                ?.findBottom(sticky)
                ?.takeIf { it > threshold }
                .also { sticky.view.isVisible = it != null }
                ?: continue
            var bottom = min(holderBottom, threshold)
            holders.findBarrier(sticky.position, sticky.ref)?.let { barrier ->
                val top = bottom - sticky.view.measuredHeight
                bottom += min(0, top - barrier)
            }
            sticky.view.translationY = (threshold - bottom).toFloat()
        }
    }

    private fun List<HolderInfo>.moveOriginal(position: Int): Boolean {
        val holder = find { it.position == position }
        holder ?: return false
        var offset = threshold - holder.view.bottom
        if (offset >= 0) {
            offset = 0
        } else findBarrier(position, holder.ref)?.let { barrier ->
            val top = threshold - holder.view.height
            offset -= min(0, top - barrier)
        }
        holder.view.translationY = offset.toFloat()
        return true
    }

    /** @return some holder to move sticky with */
    private fun List<HolderInfo>.findBottom(sticky: StickyBottom): Int? {
        // don't use item.children
        val openedId = sticky.getOpenedId()
        for (holder in this) {
            if (holder.ref.parent != sticky.ref) continue
            val index = sticky.sortedChildren.indexOfFirst { it.uniqueId == holder.uniqueId }
            val openedIndex = sticky.sortedChildren.indexOfFirst { it.uniqueId == openedId }
            val last = sticky.sortedChildren.lastOrNull()
            return when {
                index <= openedIndex -> continue
                holder.uniqueId == last?.uniqueId -> stickyBox.height.inc()
                else -> holder.view.bottom
            }
        }
        return null
    }

    /** @return the bottom space between of the bottom and other separator or the top of the first child */
    private fun List<HolderInfo>.findBarrier(position: Int, ref: NodeRef): Int? {
        for (i in indices) {
            val info = get(i)
            return when {
                // skip below the target
                info.position >= position -> continue
                // next separator above
                info.isSeparator -> info.view.bottom + space
                // skip closed children
                !info.isOpened && info.ref.parent == ref -> continue
                // nothing below
                i == 0 -> return null
                // the first child after children of other opened
                else -> get(i.dec()).view.top
            }
        }
        return null
    }
}