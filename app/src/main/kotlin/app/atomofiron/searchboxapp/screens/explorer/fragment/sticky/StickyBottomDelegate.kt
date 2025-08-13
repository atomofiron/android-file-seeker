package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky

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

private typealias StickyBottom = StickyInfo<ExplorerStickyBottomView>

class StickyBottomDelegate(
    private val holders: Set<Map.Entry<Int, HolderInfo>>,
    private val stickyBox: FrameLayout,
    private var listener: ExplorerItemBinderActionListener,
) {

    private val stickies = HashMap<Int, StickyBottom>()
    private val onClick: (Node) -> Unit = listener::onItemClick

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

    private fun sync(new: Node, position: Int) {
        val sticky = stickies[new.uniqueId]
        if (sticky?.item?.areContentsTheSame(new) != true) {
            val view = sticky?.view
                ?: removeSticky(new.uniqueId)
                    .let { newSticky(new) }
            view.bind(new)
            stickies[new.uniqueId] = StickyInfo(position, new, view)
        }
    }

    fun updateOffset() {
        for (sticky in stickies.values) {
            sticky.view.isVisible = false
        }
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
        stickyBox.addView(view)
        stickyBox.takeIf { it.measuredWidth > 0 }?.let {
            view.measure(
                MeasureSpec.makeMeasureSpec(it.measuredWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            )
        }
        return view
    }
}