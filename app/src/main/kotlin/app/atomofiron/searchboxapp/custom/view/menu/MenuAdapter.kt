package app.atomofiron.searchboxapp.custom.view.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.searchboxapp.custom.view.menu.holder.DangerousMenuItemHolder
import app.atomofiron.searchboxapp.custom.view.menu.holder.MenuHolder
import app.atomofiron.searchboxapp.custom.view.menu.holder.MenuItemHolder

private const val ShortItem = 1
const val LongItem = 2

class MenuAdapter : GeneralAdapter<MenuItem, MenuHolder>() {

    lateinit var menuListener: MenuListener
    val spanSizeLookup: SpanSizeLookup = SpanSizeLookupImpl()
    var spanLimit = LongItem

    init {
        setHasStableIds(true)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.requestDisallowInterceptTouchEvent(true)
    }

    override fun getItemId(position: Int): Long = items[position].id.toLong()

    override fun getItemViewType(position: Int): Int {
        return when (get(position).content) {
            MenuItemContent.Dangerous -> MenuItemType.Dangerous.viewType
            is MenuItemContent.Common -> MenuItemType.Common.viewType
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): MenuHolder {
        return when (MenuItemType(viewType)) {
            MenuItemType.Dangerous -> DangerousMenuItemHolder(parent, menuListener)
            MenuItemType.Common -> MenuItemHolder(parent, menuListener)
        }
    }

    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        holder.isLong = spanSizeLookup.getSpanSize(position) == LongItem
        holder.bind(get(position), position)
    }

    private inner class SpanSizeLookupImpl : SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return when (get(position).content) {
                is MenuItemContent.Common -> items.takeIf { position == 0 }
                    ?.indexOfLast { it.content is MenuItemContent.Dangerous }
                    ?.takeIf { it >= 0 }
                    ?.let { ShortItem + it % LongItem }
                    ?: ShortItem
                is MenuItemContent.Dangerous -> LongItem
            }.coerceAtMost(spanLimit)
        }
    }
}