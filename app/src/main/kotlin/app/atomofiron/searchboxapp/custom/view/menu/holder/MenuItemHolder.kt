package app.atomofiron.searchboxapp.custom.view.menu.holder;

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import app.atomofiron.fileseeker.databinding.ItemCurtainMenuBinding
import app.atomofiron.searchboxapp.custom.drawable.setMenuItemBackground
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuItemContent
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.model.other.get
import app.atomofiron.searchboxapp.utils.resources

class MenuItemHolder private constructor(
    private val binding: ItemCurtainMenuBinding,
    private val listener: MenuListener,
) : MenuHolder(binding.root) {

    private var itemId = 0

    constructor(parent: ViewGroup, listener: MenuListener) : this(
        ItemCurtainMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener,
    )

    init {
        binding.root.setMenuItemBackground()
        itemView.setOnClickListener {
            listener.onMenuItemSelected(itemId)
        }
    }

    override fun onBind(item: MenuItem, position: Int) {
        itemId = item.id
        val content = item.content as MenuItemContent.Common
        binding.icon.setImageResource(content.head)
        val label = item.longLabel?.takeIf { isLong } ?: item.label
        binding.label.text = binding.resources[label]
        binding.tail.isVisible = content.tail != null
        if (content.tail != null) {
            binding.tail.setImageResource(content.tail)
        }
    }
}
