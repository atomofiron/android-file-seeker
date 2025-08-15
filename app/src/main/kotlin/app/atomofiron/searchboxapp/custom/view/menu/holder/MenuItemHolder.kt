package app.atomofiron.searchboxapp.custom.view.menu.holder;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemCurtainMenuBinding
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuItemContent
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.model.other.get
import app.atomofiron.searchboxapp.utils.resources

class MenuItemHolder private constructor(
    itemView: View,
    private val listener: MenuListener,
) : MenuHolder(itemView) {

    private val binding = ItemCurtainMenuBinding.bind(itemView)

    private var itemId = 0

    constructor(parent: ViewGroup, listener: MenuListener) : this(
        LayoutInflater.from(parent.context).inflate(R.layout.item_curtain_menu, parent, false),
        listener,
    )

    init {
        itemView.setOnClickListener {
            listener.onMenuItemSelected(itemId)
        }
    }

    override fun onBind(item: MenuItem, position: Int) {
        itemId = item.id
        val content = item.content as MenuItemContent.Common
        binding.icon.setImageResource(content.head)
        binding.label.text = binding.resources[item.label]
    }
}
