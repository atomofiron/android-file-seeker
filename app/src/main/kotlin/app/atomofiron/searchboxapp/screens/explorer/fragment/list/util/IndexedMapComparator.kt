package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import android.view.View
import androidx.recyclerview.widget.RecyclerView

object IndexedMapComparator {
    val instance = Comparator<Int> { first, second -> first - second }
}

fun RecyclerView.getSortedChildren(): Map<Int, View> {
    val children = mutableMapOf<Int, View>()
    // exclude duplicated items (some items have the same adapter position)
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        val position = getChildLayoutPosition(child)
        children[position] = child
    }
    return children.toSortedMap(IndexedMapComparator.instance)
}