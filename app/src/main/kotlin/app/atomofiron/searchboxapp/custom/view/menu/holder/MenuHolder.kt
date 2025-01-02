package app.atomofiron.searchboxapp.custom.view.menu.holder

import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView

sealed class MenuHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: MenuItem)
}
