package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.custom.view.menu.MenuAdapter
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener

class MenuView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val adapter = MenuAdapter()

    init {
        overScrollMode = View.OVER_SCROLL_NEVER
        layoutManager = LinearLayoutManager(context)
        isVerticalScrollBarEnabled = false
        super.setAdapter(adapter)
    }

    fun submit(items: List<MenuItem>) = adapter.submit(items)

    fun setMenuListener(listener: MenuListener) {
        adapter.menuListener = listener
    }
}