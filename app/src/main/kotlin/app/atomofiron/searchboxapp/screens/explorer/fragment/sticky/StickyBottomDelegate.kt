package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky

import android.view.Gravity
import android.view.View.MeasureSpec
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import androidx.core.view.isVisible
import app.atomofiron.searchboxapp.custom.view.ExplorerStickyBottomView
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl.ExplorerItemBinderActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.info.HolderInfo
import app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.info.StickyInfo
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isSeparator
import app.atomofiron.searchboxapp.utils.ExplorerUtils.originalPath
import kotlin.math.max

private typealias StickyBottom = StickyInfo<ExplorerStickyBottomView>

class StickyBottomDelegate(
    private val holders: Collection<HolderInfo>,
    private val stickyBox: FrameLayout,
    private var listener: ExplorerItemBinderActionListener,
) {
    private val stickies = HashMap<Int, StickyBottom>()
    private val wrapContent = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
    private val onClick: (Node) -> Unit = listener::onItemClick
    private val threshold get() = stickyBox.paddingBottom

    fun valid(item: Node) = item.isSeparator()

    fun sync(separators: List<Pair<Int,Node>>) {
        for (sticky in stickies.entries.toList()) {
            if (!separators.any { it.second.uniqueId == sticky.value.item.uniqueId }) {
                removeSticky(sticky.key)
            }
        }
        for ((position, item) in separators) {
            sync(item, position)
        }
    }

    fun onAttach(info: HolderInfo) {
        if (info.item.isSeparator()) {
            updateOffset()
        }
    }

    fun onDetach(info: HolderInfo) {
        info.view.translationX = 0f
    }

    private fun sync(new: Node, position: Int) {
        val sticky = stickies[new.uniqueId]
        val view = when {
            sticky == null -> newSticky(new)
            sticky.position != position -> sticky.view
            !sticky.item.areContentsTheSame(new) -> sticky.view
            else -> return
        }
        stickies[new.uniqueId] = StickyInfo(position, new, view)
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
                ?.let { stickyBox.height - it }
                ?.takeIf { it < threshold }
                .also { sticky.view.isVisible = it != null }
                ?: continue
            var bottom = max(holderBottom, threshold)
            holders.findBarrier(sticky.position, sticky.item)?.let { barrier ->
                val top = bottom + sticky.view.measuredHeight
                bottom -= max(0, top - barrier)
            }
            sticky.view.translationY = (threshold - bottom).toFloat()
        }
    }

    private fun List<HolderInfo>.moveOriginal(position: Int): Boolean {
        val holder = find { it.position == position }
        holder ?: return false
        val stickyBottom = stickyBox.height - threshold
        holder.view.translationY = when {
            holder.view.bottom <= stickyBottom -> 0f
            else -> {
                var offset = stickyBottom - holder.view.bottom
                findBarrier(position, holder.item)?.let { barrier ->
                    val top = threshold + holder.view.height
                    offset += max(0, top - barrier)
                }
                offset.toFloat()
            }
        }
        return true
    }

    /** @return some holder to move sticky with */
    private fun List<HolderInfo>.findBottom(sticky: StickyBottom): Int? {
        val openedIndex = sticky.item.getOpenedIndex()
        for (holder in this) {
            sticky.item.children
                .takeIf { holder.item.parentPath == sticky.item.originalPath() }
                ?.indexOfFirst { it.uniqueId == holder.item.uniqueId }
                ?.takeIf { it > openedIndex }
                ?.let { return holder.view.bottom }
        }
        return null
    }

    /** @return the bottom space between of the bottom and other separator or the top of the first child */
    private fun List<HolderInfo>.findBarrier(position: Int, item: Node): Int? {
        for (i in indices) {
            val info = get(i)
            return when {
                // skip below the target
                info.position >= position -> continue
                // next separator above
                info.item.isSeparator() -> info.view.bottom
                // skip closed children
                !info.item.isOpened && info.item.parentPath == item.originalPath() -> continue
                // nothing below
                i == 0 -> return null
                // the first child after children of other opened
                else -> get(i.dec()).view.top
            }.let { stickyBox.height - it }
        }
        return null
    }
}