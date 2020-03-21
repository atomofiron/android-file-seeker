package ru.atomofiron.regextool.screens.explorer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.iss.service.explorer.model.XFile
import ru.atomofiron.regextool.screens.explorer.adapter.ItemSeparationDecorator.Separation
import ru.atomofiron.regextool.screens.explorer.adapter.ItemShadowDecorator.Shadow
import ru.atomofiron.regextool.screens.explorer.adapter.ItemSpaceDecorator.Divider

class ExplorerAdapter : GeneralAdapter<ExplorerHolder, XFile>() {
    companion object {
        private const val VIEW_TYPE = 1
        private const val VIEW_POOL_MAX_COUNT = 30
    }

    var itemActionListener: ExplorerItemActionListener? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var viewPool: Array<View?>? = null

    private var currentDir: XFile? = null

    private val spaceDecorator = ItemSpaceDecorator { i ->
        val item = items[i]
        val nextPosition = i.inc()
        when {
            item.isOpened && item.files.isNullOrEmpty() -> Divider.BIG
            item.isOpened -> Divider.SMALL
            nextPosition >= items.size -> Divider.NO // не делаем отступ у последнего элемента
            item.isRoot && items[nextPosition].isRoot -> Divider.NO
            nextPosition == items.size && item.completedParentPath == currentDir?.completedPath -> Divider.SMALL
            nextPosition != items.size && item.completedParentPath != items[nextPosition].completedParentPath ||
                    item.root != items[nextPosition].root -> Divider.SMALL
            else -> Divider.NO
        }
    }

    private val shadowDecorator = ItemShadowDecorator { position ->
        val currentDir = currentDir ?: return@ItemShadowDecorator Shadow.NO
        val item = items[position]
        val isCurrent = item.completedPath == currentDir.completedPath
        val isCurrentChild = item.completedParentPath == currentDir.completedPath
        val nextPosition = position.inc()

        when {
            !isCurrent && !isCurrentChild -> Shadow.NO
            item.isOpened && item.files.isNullOrEmpty() -> Shadow.DOUBLE
            item.isOpened -> Shadow.TOP
            nextPosition == items.size -> Shadow.NO // не рисуем под последним элементом
            item.isRoot && items[nextPosition].isRoot -> Shadow.NO
            item.completedParentPath != items[nextPosition].completedParentPath ||
                    item.root != items[nextPosition].root -> Shadow.BOTTOM
            isCurrentChild -> Shadow.TOP_SLIDE
            else -> Shadow.NO
        }
    }

    private val separationDecorator = ItemSeparationDecorator { position ->
        val currentDir = currentDir ?: return@ItemSeparationDecorator Separation.NO
        val item = items[position]
        val nextPosition = position.inc()

        when {
            item.completedPath == currentDir.completedPath -> Separation.NO
            item.completedParentPath == currentDir.completedPath -> Separation.NO
            item.root != currentDir.root -> Separation.NO
            item.completedPath == currentDir.completedPath -> Separation.NO
            item.isOpened && item.files.isNullOrEmpty() -> Separation.NO
            item.isOpened -> Separation.TOP
            nextPosition == items.size -> Separation.NO // не рисуем под последним элементом
            item.isRoot && items[nextPosition].isRoot -> Separation.NO
            currentDir.isRoot -> Separation.NO
            item.completedParentPath != items[nextPosition].completedParentPath ||
                    item.root != items[nextPosition].root -> Separation.BOTTOM
            else -> Separation.NO
        }
    }

    fun setCurrentDir(dir: XFile?) {
        currentDir = dir
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.itemAnimator = null
        recyclerView.recycledViewPool.setMaxRecycledViews(VIEW_TYPE, VIEW_POOL_MAX_COUNT)

        viewPool = arrayOfNulls(VIEW_POOL_MAX_COUNT)
        val inflater = LayoutInflater.from(recyclerView.context)
        for (i in viewPool!!.indices) {
            viewPool!![i] = inflateNewView(inflater, recyclerView)
        }

        recyclerView.addItemDecoration(spaceDecorator)
        recyclerView.addItemDecoration(shadowDecorator)
        recyclerView.addItemDecoration(separationDecorator)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        recyclerView.removeItemDecoration(spaceDecorator)
        recyclerView.removeItemDecoration(shadowDecorator)
        recyclerView.removeItemDecoration(separationDecorator)
    }

    override fun getItemViewType(position: Int): Int = VIEW_TYPE

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): ExplorerHolder {
        val view = getNewView(inflater, parent)

        return ExplorerHolder(view)
    }

    override fun onBindViewHolder(holder: ExplorerHolder, position: Int) {
        holder.onItemActionListener = itemActionListener
        super.onBindViewHolder(holder, position)
        itemActionListener?.onItemVisible(holder.item)
    }

    override fun onViewDetachedFromWindow(holder: ExplorerHolder) {
        super.onViewDetachedFromWindow(holder)
        itemActionListener?.onItemInvalidate(holder.item)
    }

    private fun getNewView(inflater: LayoutInflater, parent: ViewGroup): View {
        val viewPool = viewPool
        if (viewPool != null) {
            for (i in viewPool.indices) {
                val view = viewPool[i]
                if (view != null) {
                    viewPool[i] = null
                    return view
                }
            }
            this.viewPool = null
        }
        return inflateNewView(inflater, parent)
    }

    private fun inflateNewView(inflater: LayoutInflater, parent: ViewGroup): View {
        return inflater.inflate(R.layout.item_explorer_file, parent, false)
    }
}