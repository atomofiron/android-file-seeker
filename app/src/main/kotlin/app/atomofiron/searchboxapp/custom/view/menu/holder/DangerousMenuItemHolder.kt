package app.atomofiron.searchboxapp.custom.view.menu.holder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemCurtainMenuDangerousBinding
import app.atomofiron.searchboxapp.custom.view.dangerous.DangerousSliderView
import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
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
        val deepBlack = view.context.findBooleanByAttr(R.attr.isBlackDeep)
        val bordered = !view.resources.getBoolean(R.bool.isDayTheme) && deepBlack
        view.setThumbBorder(bordered)
        val thumbColor = when {
            deepBlack -> Color.TRANSPARENT
            else -> view.context.findColorByAttr(MaterialAttr.colorSurfaceContainer)
        }
        view.setThumbColor(thumbColor)
    }

    override fun onBind(item: MenuItem, position: Int) {
        itemId = item.id
        val title = view.resources[item.label]
        view.setText(title)
        view.resources
            .getString(R.string.slide_to)
            .replace(Const.PLACEHOLDER, title.lowercase())
            .let { view.setTip(it) }
    }
}
