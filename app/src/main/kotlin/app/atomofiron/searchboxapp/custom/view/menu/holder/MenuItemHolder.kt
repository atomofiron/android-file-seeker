package app.atomofiron.searchboxapp.custom.view.menu.holder;

import android.graphics.drawable.RippleDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemCurtainMenuBinding
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuItemContent
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.model.other.get
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.context
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
        itemView.setOnClickListener {
            listener.onMenuItemSelected(itemId)
        }
        (binding.root.background as RippleDrawable).run {
            val deepBlack = binding.context.findBooleanByAttr(R.attr.isBlackDeep)
            val darkTheme = binding.resources.getBoolean(R.bool.isNightTheme)
            findDrawableByLayerId(R.id.fill).alpha = Alpha.visibleInt(!darkTheme || !deepBlack)
            findDrawableByLayerId(R.id.stroke).alpha = Alpha.visibleInt(darkTheme && deepBlack)
        }
    }

    override fun onBind(item: MenuItem, position: Int) {
        itemId = item.id
        val content = item.content as MenuItemContent.Common
        binding.icon.setImageResource(content.head)
        binding.label.text = binding.resources[item.label]
    }
}
