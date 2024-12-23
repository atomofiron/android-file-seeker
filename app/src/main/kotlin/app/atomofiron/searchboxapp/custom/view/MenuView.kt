package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import android.view.MenuInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.custom.view.menu.MenuAdapter
import app.atomofiron.searchboxapp.custom.view.menu.MenuImpl
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener

class MenuView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val menuAdapter = MenuAdapter(context)

    init {
        overScrollMode = View.OVER_SCROLL_NEVER
        layoutManager = LinearLayoutManager(context)
        adapter = menuAdapter
        isVerticalScrollBarEnabled = false
    }

    fun inflateMenu(menuResId: Int): MenuImpl {
        MenuInflater(context).inflate(menuResId, menuAdapter.menu)
        return menuAdapter.menu
    }

    fun setMenuListener(listener: MenuListener) {
        menuAdapter.menuListener = listener
    }

    fun markAsDangerous(itemId: Int) = menuAdapter.markAsDangerous(itemId)
}