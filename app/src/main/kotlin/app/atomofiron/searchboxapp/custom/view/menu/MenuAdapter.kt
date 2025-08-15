package app.atomofiron.searchboxapp.custom.view.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.searchboxapp.custom.view.menu.holder.DangerousMenuItemHolder
import app.atomofiron.searchboxapp.custom.view.menu.holder.MenuHolder
import app.atomofiron.searchboxapp.custom.view.menu.holder.MenuItemHolder

class MenuAdapter : GeneralAdapter<MenuItem, MenuHolder>() {

    lateinit var menuListener: MenuListener

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

    override fun onBindViewHolder(holder: MenuHolder, position: Int) = holder.bind(get(position))
}