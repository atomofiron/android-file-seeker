package app.atomofiron.searchboxapp.custom.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuAdapter
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import kotlin.math.max

class MenuView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val minColumnWidth = resources.getDimensionPixelSize(R.dimen.min_portrait_screen_half)
    private val adapter = MenuAdapter()

    init {
        overScrollMode = View.OVER_SCROLL_NEVER
        layoutManager = GridLayoutManager(context, 2)
            .apply { spanSizeLookup = adapter.spanSizeLookup }
        isVerticalScrollBarEnabled = false
        super.setAdapter(adapter)
    }

    fun submit(items: List<MenuItem>) = adapter.submit(items)

    fun setMenuListener(listener: MenuListener) {
        adapter.menuListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        max(1, width / minColumnWidth)
            .takeIf { it != adapter.spanLimit }
            ?.let { adapter.spanLimit = it }
            ?.let { adapter.notifyDataSetChanged() }
    }
}