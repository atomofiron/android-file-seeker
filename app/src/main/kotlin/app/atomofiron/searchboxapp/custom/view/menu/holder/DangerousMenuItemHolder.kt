package app.atomofiron.searchboxapp.custom.view.menu.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemCurtainMenuDangerousBinding
import app.atomofiron.searchboxapp.custom.drawable.colorSurfaceContainer
import app.atomofiron.searchboxapp.custom.view.dangerous.DangerousSliderView
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.custom.view.menu.MenuItemContent
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.model.other.get
import app.atomofiron.searchboxapp.utils.Const

class DangerousMenuItemHolder private constructor(
    val view: DangerousSliderView,
    private val listener: MenuListener,
) : MenuHolder(view) {

    private var itemId = 0

    constructor(parent: ViewGroup, listener: MenuListener) : this(
        ItemCurtainMenuDangerousBinding.inflate(LayoutInflater.from(parent.context), parent, false).root,
        listener,
    )

    init {
        view.listener = {
            listener.onMenuItemSelected(itemId)
            true
        }
        view.setThumbColor(view.context.colorSurfaceContainer())
        view.setStartBorder(view.context.colorSurfaceContainer())
        view.updatePaddingRelative(end = view.paddingEnd + view.resources.getDimensionPixelSize(R.dimen.padding_mini))
    }

    override fun onBind(item: MenuItem, position: Int) {
        itemId = item.id
        val title = view.resources[item.label]
        val content = item.content as MenuItemContent.Dangerous
        view.setText(title)
        view.setIcon(content.icon)
        view.resources
            .getString(R.string.slide_to)
            .replace(Const.PLACEHOLDER, title.lowercase())
            .let { view.setTip(it) }
    }
}
