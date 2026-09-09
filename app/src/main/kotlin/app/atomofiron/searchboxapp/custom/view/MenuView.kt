package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.menu.LongItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuAdapter
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.custom.view.menu.ShortItem

class MenuView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val minColumnWidth = resources.getDimensionPixelSize(R.dimen.min_portrait_screen_half)
    private val adapter = MenuAdapter()
    private val gridLayoutManager = GridLayoutManager(context, 2)

    init {
        overScrollMode = OVER_SCROLL_NEVER
        layoutManager = gridLayoutManager
        gridLayoutManager.spanSizeLookup = adapter.spanSizeLookup
        isVerticalScrollBarEnabled = false
        super.setAdapter(adapter)
    }

    fun submit(items: List<MenuItem>) = adapter.submit(items)

    fun setMenuListener(listener: MenuListener) {
        adapter.menuListener = listener
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)

        val width = MeasureSpec.getSize(widthSpec)
        val spanCount = (width / minColumnWidth).coerceIn(ShortItem..LongItem)
        gridLayoutManager.spanCount = spanCount
        adapter.spanLimit = spanCount
    }
}