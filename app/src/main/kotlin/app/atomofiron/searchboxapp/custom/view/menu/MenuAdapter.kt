package app.atomofiron.searchboxapp.custom.view.menu

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.custom.view.menu.holder.DangerousMenuItemHolder
import app.atomofiron.searchboxapp.custom.view.menu.holder.MenuHolder
import app.atomofiron.searchboxapp.custom.view.menu.holder.MenuItemHolder

private const val TYPE_NORMAL = 1
private const val TYPE_DANGEROUS = 2

class MenuAdapter(context: Context) : RecyclerView.Adapter<MenuHolder>() {
    val menu = MenuImpl(context)

    lateinit var menuListener: MenuListener
    private var dangerousItemId = 0

    init {
        setHasStableIds(true)
        menu.setMenuChangedListener(::notifyDataSetChanged)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.requestDisallowInterceptTouchEvent(true)
    }

    override fun getItemId(position: Int): Long = menu.getItem(position).itemId.toLong()

    override fun getItemViewType(position: Int): Int = when (dangerousItemId) {
        menu.getItem(position).itemId -> TYPE_DANGEROUS
        else -> TYPE_NORMAL
    }

    fun markAsDangerous(itemId: Int) {
        dangerousItemId = itemId
        val index = menu.findItemIndex(itemId)
        notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuHolder {
        return when (viewType) {
            TYPE_DANGEROUS -> DangerousMenuItemHolder(parent, menuListener)
            else -> MenuItemHolder(parent, menuListener)
        }
    }

    override fun getItemCount(): Int = menu.size()

    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        holder.bind(menu.getItem(position))
    }
}