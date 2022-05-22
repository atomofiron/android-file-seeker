package app.atomofiron.searchboxapp.custom.view.bottom_sheet_menu

import android.content.Context
import android.view.MenuInflater
import android.view.View
import androidx.core.view.iterator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.custom.view.bottom_sheet.BottomSheetDelegate

open class BottomSheetMenu(
    layoutContent: Int = UNDEFINED,
    context: Context,
    private val menuId: Int,
    private val menuItemClickListener: BottomSheetMenuListener
) : BottomSheetDelegate(layoutContent) {
    protected open val recyclerView: RecyclerView = RecyclerView(context)
    @Suppress("SuspiciousVarProperty")
    override var contentView: View? = null
        get() = recyclerView

    private val menuAdapter = BottomSheetMenuAdapter(context)
    private val menuImpl = menuAdapter.menu

    init {
        menuAdapter.menuItemClickListener = Listener()
    }

    override fun onViewReady() {
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = menuAdapter
    }

    open fun show(items: List<Int>) {
        menuImpl.clear()
        MenuInflater(context).inflate(menuId, menuImpl)
        val it = menuImpl.iterator()
        while (it.hasNext()) {
            val item = it.next()
            if (!items.contains(item.itemId)) {
                it.remove()
            }
        }
        super.show()
    }

    private inner class Listener : BottomSheetMenuListener {
        override fun onMenuItemSelected(id: Int) {
            hide()
            menuItemClickListener.onMenuItemSelected(id)
        }
    }
}