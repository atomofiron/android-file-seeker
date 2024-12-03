package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.Node.Companion.toUniqueId
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.*
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator.ItemBackgroundDecorator
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator.ItemBorderDecorator
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator.RootItemMarginDecorator
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.withoutDot
import kotlin.math.min

class ExplorerListDelegate(
    private val recyclerView: RecyclerView,
    private val rootAdapter: RootAdapter,
    private val nodeAdapter: ExplorerAdapter,
    headerView: ExplorerHeaderView,
    private val output: ExplorerItemActionListener,
) {
    private var currentDir: Node? = null

    private val headerDelegate = ExplorerHeaderDelegate(recyclerView, headerView, nodeAdapter)
    private val rootMarginDecorator = RootItemMarginDecorator()
    private val backgroundDecorator = ItemBackgroundDecorator(evenNumbered = true)
    private val borderDecorator = ItemBorderDecorator(nodeAdapter, headerView, headerDelegate::onDecoratorDraw)

    init {
        recyclerView.addItemDecoration(rootMarginDecorator)
        recyclerView.addItemDecoration(backgroundDecorator)
        recyclerView.addItemDecoration(borderDecorator)
        headerView.setOnItemActionListener(HeaderListener())
    }

    private fun getFirstChild(): View? = recyclerView.getChildAt(0)

    private fun getLastChild(): View? = recyclerView.getChildAt(recyclerView.childCount.dec())

    fun isCurrentDirVisible(): Boolean? {
        val current = currentDir ?: return null
        return isVisible(current)
    }

    fun isVisible(item: Node): Boolean {
        val path = item.withoutDot()
        val index = nodeAdapter.currentList.indexOfFirst { it.path == path }
        return isVisible(index + rootAdapter.itemCount)
    }

    fun isVisible(position: Int): Boolean {
        val firstChild = getFirstChild() ?: return false
        val lastChild = getLastChild() ?: return false
        var topItemPosition = recyclerView.getChildLayoutPosition(firstChild)
        val bottomItemPosition = recyclerView.getChildLayoutPosition(lastChild)
        if (firstChild.top < 0) topItemPosition = min(bottomItemPosition, topItemPosition.inc())
        return position in topItemPosition..bottomItemPosition
    }

    fun setCurrentDir(item: Node?) {
        currentDir = item
        borderDecorator.setCurrentDir(item)
        headerDelegate.setCurrentDir(item)
    }

    fun setComposition(composition: ExplorerItemComposition) {
        backgroundDecorator.enabled = composition.visibleBg
        headerDelegate.setComposition(composition)
    }

    fun scrollTo(item: Node) {
        val path = item.withoutDot()
        var lastChild = getLastChild() ?: return
        var position = nodeAdapter.currentList.indexOfFirst { it.path == path }
        position += rootAdapter.itemCount
        val lastItemPosition = recyclerView.getChildLayoutPosition(lastChild)
        recyclerView.stopScroll()
        when {
            position > lastItemPosition -> {
                recyclerView.scrollToPosition(position.dec())
                recyclerView.post {
                    lastChild = getLastChild() ?: return@post
                    recyclerView.smoothScrollBy(0, lastChild.height * 2)
                }
            }
            else -> {
                recyclerView.scrollToPosition(position.inc())
                recyclerView.post {
                    val firstChild = getFirstChild() ?: return@post
                    recyclerView.smoothScrollBy(0, -firstChild.height * 3 / 2)
                }
            }
        }
    }

    fun highlight(item: Node) {
        val uniqueId = item.withoutDot().toUniqueId()
        val dir = nodeAdapter.currentList.find { it.uniqueId == uniqueId }
        dir ?: return
        val holder = recyclerView.findViewHolderForItemId(dir.uniqueId.toLong())
        if (holder !is ExplorerHolder) return
        val scrollOffset = recyclerView.paddingTop - holder.itemView.top
        if (scrollOffset > 0) {
            recyclerView.smoothScrollBy(0, -scrollOffset)
        }
        holder.highlight()
    }

    private inner class HeaderListener : ExplorerItemActionListener {
        override fun onItemVisible(item: Node) = Unit // unreachable

        override fun onItemLongClick(item: Node) = output.onItemLongClick(item)

        override fun onItemCheck(item: Node, isChecked: Boolean) = output.onItemCheck(item, isChecked)

        override fun onItemClick(item: Node) = when (isCurrentDirVisible()) {
            true -> output.onItemClick(item)
            false -> scrollTo(item)
            null -> Unit
        }
    }
}