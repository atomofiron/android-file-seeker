package app.atomofiron.searchboxapp.screens.finder.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem

class FinderDiffUtilCallback : DiffUtil.ItemCallback<FinderStateItem>() {

    override fun areItemsTheSame(oldItem: FinderStateItem, newItem: FinderStateItem): Boolean = when {
        oldItem.layoutId != newItem.layoutId -> false
        oldItem.stableId != newItem.stableId -> false
        else -> true
    }

    @SuppressLint("DiffUtilEquals")
    // all child classes are 'data classes'
    override fun areContentsTheSame(oldItem: FinderStateItem, newItem: FinderStateItem): Boolean = oldItem == newItem
}