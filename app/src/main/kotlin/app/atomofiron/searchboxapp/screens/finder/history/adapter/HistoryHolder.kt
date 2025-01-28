package app.atomofiron.searchboxapp.screens.finder.history.adapter

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R

class HistoryHolder(itemView: View, onItemActionListener: OnItemActionListener) : RecyclerView.ViewHolder(itemView) {
    companion object {
        private const val UNDEFINED = -1
    }
    private val btnPinned = itemView.findViewById<ImageButton>(R.id.item_history_btn_pinned)
    private val tvTitle = itemView.findViewById<TextView>(R.id.item_history_tv_title)
    private val btnRemove = itemView.findViewById<ImageButton>(R.id.item_history_btn_remove)

    init {
        itemView.setOnClickListener {
            if (absoluteAdapterPosition == UNDEFINED) return@setOnClickListener
            onItemActionListener.onItemClick(absoluteAdapterPosition)
        }
        btnPinned.setOnClickListener {
            if (absoluteAdapterPosition == UNDEFINED) return@setOnClickListener
            onItemActionListener.onItemPin(absoluteAdapterPosition)
        }
        btnRemove.setOnClickListener {
            if (absoluteAdapterPosition == UNDEFINED) return@setOnClickListener
            onItemActionListener.onItemRemove(absoluteAdapterPosition)
        }
    }

    fun onBind(title: String, pinned: Boolean) {
        btnPinned.isActivated = pinned
        tvTitle.text = title
        btnRemove.isGone = pinned
    }

    interface OnItemActionListener {
        fun onItemRemove(position: Int)
        fun onItemClick(position: Int)
        fun onItemPin(position: Int)
    }
}