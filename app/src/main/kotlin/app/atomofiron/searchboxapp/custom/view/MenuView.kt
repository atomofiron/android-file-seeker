package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.custom.view.menu.MenuAdapter
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuItemContent
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener

class MenuView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val adapter = MenuAdapter()

    init {
        overScrollMode = View.OVER_SCROLL_NEVER
        layoutManager = GridLayoutManager(context, 2)
            .apply { spanSizeLookup = SpanSizeLookupImpl() }
        isVerticalScrollBarEnabled = false
        super.setAdapter(adapter)
    }

    fun submit(items: List<MenuItem>) = adapter.submit(items)

    fun setMenuListener(listener: MenuListener) {
        adapter.menuListener = listener
    }

    private inner class SpanSizeLookupImpl : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return when (adapter[position].content) {
                is MenuItemContent.Common -> 1
                is MenuItemContent.Dangerous -> 2
            }
        }
    }
}