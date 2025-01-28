package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerAdapter
import java.util.LinkedList
import kotlin.math.max
import kotlin.math.min

class ExplorerHeaderDelegate(
    private val recyclerView: RecyclerView,
    private val headerView: ExplorerHeaderView,
    private val adapter: ExplorerAdapter,
) : RecyclerView.OnScrollListener(), View.OnLayoutChangeListener {

    private var currentDir: Node? = null

    private var composition: ExplorerItemComposition? = null
    private var updateOnDecoratorDraw = true
    private var bindingRequired = true

    init {
        recyclerView.addOnScrollListener(this)
        recyclerView.addOnLayoutChangeListener(this)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (bindingRequired) tryBind()
        if (dy != 0) {
            updateOnDecoratorDraw = false
            updateOffset()
        }
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        tryBind()
    }

    fun setCurrentDir(item: Node?) {
        bindingRequired = bindingRequired || currentDir?.areContentsTheSame(item) != true
        currentDir = item
    }

    fun onDecoratorDraw() {
        if (updateOnDecoratorDraw) {
            updateOffset()
        }
    }

    private fun tryBind() {
        val composition = composition ?: return
        val currentDir = currentDir ?: return

        headerView.setComposition(composition)
        headerView.bind(currentDir)
        bindingRequired = false
    }

    private fun updateOffset() {
        val bottom = when (currentDir) {
            null -> 0
            else -> getHeaderBottom()
        }
        headerView.move(bottom)
    }

    private fun getHeaderBottom(): Int {
        val headerHeight = headerView.height
        val currentDir = currentDir ?: return 0
        if (currentDir.isEmpty) return 0
        val topChildren = LinkedList<View>()
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            if (child.id != R.id.item_explorer) continue
            if (child.top > headerHeight) break
            topChildren.add(child)
        }
        if (topChildren.isEmpty()) return 0
        val topItems = topChildren.map {
            val holder = recyclerView.getChildViewHolder(it)
            adapter.currentList[holder.bindingAdapterPosition]
        }
        var bottom = 0
        topItems.forEachIndexed { i, it ->
            val view = topChildren[i]
            when {
                it.parentPath == currentDir.path -> bottom = view.bottom
                it.path != currentDir.path -> Unit
                view.bottom <= headerHeight -> bottom = max(headerHeight, view.bottom)
            }
        }
        return min(headerHeight, bottom)
    }

    override fun onLayoutChange(view: View, l: Int, t: Int, r: Int, b: Int, oL: Int, oT: Int, oR: Int, oB: Int) {
        updateOnDecoratorDraw = true
    }
}