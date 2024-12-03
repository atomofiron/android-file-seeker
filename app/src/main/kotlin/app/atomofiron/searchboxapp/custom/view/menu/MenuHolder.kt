package app.atomofiron.searchboxapp.custom.view.menu

import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import app.atomofiron.searchboxapp.R

class MenuHolder(
    itemView: View,
    private val listener: MenuListener
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    companion object {
        private const val CONFIRM_BUTTON_DURATION = 1024L
    }
    val icon: ImageView = itemView.findViewById(R.id.item_menu_iv_icon)
    val title: TextView = itemView.findViewById(R.id.item_menu_tv_label)
    val button: TextView = itemView.findViewById(R.id.item_menu_tv_button)

    private lateinit var item: MenuItem
    private var clickCount = 0

    init {
        itemView.setOnClickListener(this)
        button.setOnClickListener(this)
    }

    fun bind(item: MenuItem) {
        this.item = item
        itemView.id = item.itemId
        icon.setImageDrawable(item.icon)
        title.text = item.title
        button.isGone = true
    }

    override fun onClick(view: View) {
        when(view.id) {
            itemView.id -> when {
                item.hasSubMenu() -> showButton()
                else -> listener.onMenuItemSelected(item.itemId)
            }
            R.id.item_menu_tv_button -> if (view.isEnabled) {
                button.isEnabled = false
                view.isGone = true
                listener.onMenuItemSelected(item.itemId)
            }
        }
    }

    private fun showButton() {
        val parent = itemView as ViewGroup
        if (!button.isVisible) {
            TransitionManager.beginDelayedTransition(parent)
            button.isEnabled = true
            button.isVisible = true
        }
        val count = ++clickCount
        button.postDelayed({
            if (count == clickCount && button.isVisible) {
                TransitionManager.beginDelayedTransition(parent)
                button.isVisible = false
            }
        }, CONFIRM_BUTTON_DURATION)
    }
}