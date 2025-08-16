package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.CoroutineListDiffer
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.Node.Companion.toUniqueId
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.*
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator.ItemBackgroundDecorator
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator.ItemBorderDecorator
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator.RootItemMarginDecorator
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl.ExplorerItemBinderActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.ExplorerStickyDelegate
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isSeparator
import app.atomofiron.searchboxapp.utils.ExplorerUtils.withoutDot
import lib.atomofiron.insets.attachInsetsListener
import kotlin.math.min

class ExplorerListDelegate(
    private val recyclerView: RecyclerView,
    private val rootAdapter: RootAdapter,
    private val nodeAdapter: ExplorerAdapter,
    stickyBox: FrameLayout,
    private val output: ExplorerItemActionListener,
) : CoroutineListDiffer.ListListener<Node> {

    private val stickyDelegate = ExplorerStickyDelegate(recyclerView, rootAdapter, nodeAdapter, stickyBox, StickyListener())
    private val rootMarginDecorator = RootItemMarginDecorator(recyclerView.resources)
    private val backgroundDecorator = ItemBackgroundDecorator(evenNumbered = true)
    private val borderDecorator = ItemBorderDecorator(recyclerView.context, nodeAdapter)

    private var deepestDir: Node? = null
    private val items get() = nodeAdapter.items

    init {
        recyclerView.attachInsetsListener(rootMarginDecorator)
        recyclerView.addItemDecoration(rootMarginDecorator)
        recyclerView.addItemDecoration(backgroundDecorator)
        recyclerView.addItemDecoration(borderDecorator)
        nodeAdapter.addListListener(this)
    }

    override fun onCurrentListChanged(current: List<Node>) {
        val old = deepestDir
        val new = current.find { it.isDeepest }
        when {
            new == null && old == null -> Unit
            new?.areContentsTheSame(old) == true -> Unit
            else -> setDeepestDir(new)
        }
    }

    private fun getFirstChild(offset: Int = 0): View? = recyclerView.getChildAt(offset)

    private fun getLastChild(offset: Int = 0): View? = recyclerView.getChildAt(recyclerView.childCount.dec() + offset)

    fun isDeepestDirVisible(): Boolean? = deepestDir?.let { isVisible(it) }?.takeIf { !it || deepestDir?.isRoot == false }

    fun isVisible(item: Node): Boolean {
        val path = item.withoutDot()
        val index = items.indexOfFirst { it.path == path }
        return isVisible(index + rootAdapter.itemCount)
    }

    fun isVisible(position: Int): Boolean {
        var firstChild = getFirstChild() ?: return false
        var lastChild = getLastChild() ?: return false
        var offset = 0
        while (firstChild.top < recyclerView.paddingTop) {
            firstChild = getFirstChild(++offset) ?: break
        }
        offset = 0
        while (lastChild.bottom > recyclerView.run { height - paddingBottom }) {
            lastChild = getLastChild(--offset) ?: break
        }
        var topItemPosition = recyclerView.getChildLayoutPosition(firstChild)
        val bottomItemPosition = recyclerView.getChildLayoutPosition(lastChild)
        if (firstChild.top < 0) topItemPosition = min(bottomItemPosition, topItemPosition.inc())
        return position in topItemPosition..bottomItemPosition
    }

    private fun setDeepestDir(item: Node?) {
        if (deepestDir == null && item?.isRoot == false) {
            recyclerView.post { scrollTo(item) }
        }
        deepestDir = item
        borderDecorator.setDeepestDir(item)
    }

    fun setComposition(composition: ExplorerItemComposition) {
        backgroundDecorator.enabled = composition.visibleBg
        stickyDelegate.setComposition(composition)
    }

    fun scrollTo(item: Node) {
        val targetPath = item.withoutDot()
        val nodePosition = items.indexOfFirst { it.path == targetPath }
        val position = nodePosition + rootAdapter.itemCount
        recyclerView.findViewHolderForAdapterPosition(position)
            ?.takeIf { recyclerView.run { it.itemView.top > paddingTop && it.itemView.bottom < height - paddingBottom } }
            ?.let { return }
        val middleChild = getFirstChild(recyclerView.childCount / 2) ?: return
        val space = recyclerView.resources.getDimensionPixelSize(R.dimen.explorer_item_space)
        val middlePosition = recyclerView.getChildAdapterPosition(middleChild)
        recyclerView.stopScroll()
        when {
            position > middlePosition -> {
                recyclerView.scrollToPosition(position.dec())
                recyclerView.post {
                    val holder = recyclerView.findViewHolderForAdapterPosition(position.dec())
                    holder ?: return@post
                    val area = recyclerView.run { height - paddingTop - paddingBottom}
                    val childCount = item.children?.size ?: 0
                    val dy = (holder.itemView.height * (childCount.inc()) + space * 2).coerceAtMost(area)
                    if (dy > 0) recyclerView.smoothScrollBy(0, dy)
                }
            }
            else -> {
                recyclerView.scrollToPosition(position.inc())
                recyclerView.post {
                    val holder = recyclerView.findViewHolderForLayoutPosition(position.inc())
                    holder ?: return@post
                    val dy = (recyclerView.paddingTop + holder.itemView.height) - holder.itemView.top + space
                    if (dy > 0) recyclerView.smoothScrollBy(0, -dy)
                }
            }
        }
    }

    fun highlight(item: Node) {
        val uniqueId = item.withoutDot().toUniqueId()
        val dir = items.find { it.uniqueId == uniqueId }
        dir ?: return
        val holder = recyclerView.findViewHolderForItemId(dir.uniqueId.toLong())
        if (holder !is ExplorerHolder) return
        val scrollOffset = recyclerView.paddingTop - holder.itemView.top
        if (scrollOffset > 0) {
            recyclerView.smoothScrollBy(0, -scrollOffset)
        }
        holder.highlight()
    }

    private inner class StickyListener : ExplorerItemBinderActionListener {

        override fun onItemLongClick(item: Node) = output.onItemLongClick(item)

        override fun onItemCheck(item: Node, isChecked: Boolean) = output.onItemCheck(item, isChecked)

        override fun onItemClick(item: Node) = when {
            item.isSeparator() -> highlight(item)
            isVisible(item) -> output.onItemClick(item)
            else -> scrollTo(item)
        }
    }
}