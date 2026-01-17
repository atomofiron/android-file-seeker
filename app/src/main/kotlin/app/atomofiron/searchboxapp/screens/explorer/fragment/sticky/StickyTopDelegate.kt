package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky

import android.view.View.MeasureSpec
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import androidx.core.view.isVisible
import app.atomofiron.common.util.extension.indexOfFirst
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.ExplorerStickyTopView
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinder.ExplorerItemBinderActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.info.HolderInfo
import app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.info.StickyInfo
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isSeparator
import kotlin.math.max
import kotlin.math.roundToInt

private typealias StickyTop = StickyInfo<ExplorerStickyTopView>

class StickyTopDelegate(
    private val holders: Collection<HolderInfo>,
    private val stickyBox: FrameLayout,
    private var listener: ExplorerItemBinderActionListener,
) {
    private val stickies = HashMap<Int, StickyTop>()
    private val threshold get() = stickyBox.paddingTop
    private var composition: ExplorerItemComposition? = null
    private val space = stickyBox.resources.getDimensionPixelSize(R.dimen.padding_nano)
    private val lastChildOffset = stickyBox.resources.run {
        getDimension(R.dimen.explorer_item_space) + getDimension(R.dimen.explorer_border_width) / 2
    }.roundToInt()

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        for (sticky in stickies.values) {
            sticky.view.bind(composition = composition)
        }
    }

    fun valid(item: Node) = item.isOpened && item.isEmpty == false && !item.isSeparator()

    fun getDeepest() = stickies.values.find { it.isDeepest }?.view

    fun sync(opened: List<Pair<Int,Node>>, items: List<Node>) {
        for (sticky in stickies.entries.toList()) {
            if (opened.none { it.second.uniqueId == sticky.value.uniqueId }) {
                removeSticky(sticky.key)
            }
        }
        for ((position, item) in opened) {
            sync(item, position, items)
        }
    }

    fun sync(new: Node, position: Int, items: List<Node>) {
        val sticky = stickies[new.uniqueId]
        var view = when {
            sticky == null -> null
            sticky.position != position -> sticky.view
            sticky.isDeepest != new.isDeepest -> null.also { removeSticky(new.uniqueId) }
            !sticky.areContentsTheSame(new) -> sticky.view.apply { bind(new) }
            else -> return
        }
        view = view ?: newSticky(new)
        val sorted = items.takeChildrenOf(new, position)
        stickies[new.uniqueId] = StickyInfo(position, new, view, sorted)
    }

    private fun removeSticky(uniqueId: Int) {
        stickies.remove(uniqueId)?.let {
            stickyBox.removeView(it.view)
        }
    }

    private fun newSticky(new: Node): ExplorerStickyTopView {
        val view = ExplorerStickyTopView(stickyBox.context, listener)
        view.bind(new, composition)
        view.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        stickyBox.addView(view)
        stickyBox.takeIf { it.measuredWidth > 0 }?.let {
            view.measure(
                MeasureSpec.makeMeasureSpec(it.measuredWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            )
        }
        view.translationY = -stickyBox.height.toFloat()
        return view
    }

    fun updateOffset() {
        val holders = holders.sortedBy { it.position }
        val stickies = stickies.values.sortedBy { -it.position }
        val last = holders.lastOrNull() ?: return
        for (sticky in stickies) {
            val holderTop = holders.takeIf { sticky.position <= last.position }
                ?.findTop(sticky)
                ?.takeIf { it < threshold }
                .also { sticky.view.isVisible = it != null }
                ?: continue
            var top = max(holderTop, threshold)
            holders.findBarrier(sticky)?.let { barrier ->
                val bottom = top + sticky.view.measuredHeight
                top -= max(0, bottom - barrier)
            }
            sticky.view.translationY = top - threshold.toFloat()
            sticky.view.drawTop(top - holderTop)
        }
    }

    /** @return some holder to move sticky with, the same opened dir or some child above the next opened */
    private fun List<HolderInfo>.findTop(sticky: StickyTop): Int? {
        find { it.position == sticky.position }
            ?.let { return it.view.top }
        // don't use item.children
        val openedId = sticky.getOpenedId()
        val limitIndex = sticky.sortedChildren.indexOfFirst(orElse = sticky.sortedChildren.size) {
            it.uniqueId == openedId || it.parent != sticky.ref
        }
        for (holder in this) {
            sticky.sortedChildren.takeIf { !holder.isSeparator && holder.ref.parent == sticky.ref }
                ?.indexOfFirst { it.uniqueId == holder.uniqueId }
                ?.takeIf { it in 0..<limitIndex }
                ?.let { return holder.view.top }
        }
        return null
    }

    /** @return the top of the next opened dir or the bottom of the last child */
    private fun List<HolderInfo>.findBarrier(sticky: StickyTop): Int? {
        for (i in indices) {
            val info = get(i)
            return when {
                // skip above the target
                info.position <= sticky.position -> continue
                // next opened dir below
                !sticky.isDeepest && info.isOpened -> info.view.top - space
                // skip children
                info.ref.parent == sticky.ref -> continue
                // nothing above
                i == 0 -> return null
                // the last child
                else -> get(i.dec()).view.bottom + lastChildOffset
            }
        }
        return null
    }
}