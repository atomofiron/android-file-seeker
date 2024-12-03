package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.atomofiron.searchboxapp.custom.ExplorerView

class PageHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class ExplorerPagerAdapter(
    viewPager: ViewPager2,
    output: ExplorerView.ExplorerViewOutput,
) : RecyclerView.Adapter<PageHolder>() {

    val items = Array(1/*2*/) {
        val view = ExplorerView(viewPager.context, output)
        view.tag = it.toString()
        view.layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        view
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
        val container = FrameLayout(parent.context)
        container.layoutParams = RecyclerView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        return PageHolder(container)
    }

    override fun onBindViewHolder(holder: PageHolder, position: Int) {
        val itemView = holder.itemView as ViewGroup
        var child = itemView.getChildAt(0)
        if (child?.tag != position.toString()) {
            itemView.removeAllViews()
            child = items[position]
            (child.parent as? ViewGroup)?.removeAllViews()
            itemView.addView(child)
        }
    }
}