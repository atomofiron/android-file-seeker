package app.atomofiron.searchboxapp.custom.view.menu

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.*

class MenuItemUnused(private val context: Context) : MenuItem {
    private val resources: Resources = context.resources

    private var id: Int = 0
    private var isEnabled: Boolean = true
    private var isChecked: Boolean = false
    private var isVisible: Boolean = true
    private var isCheckable: Boolean = false
    private var title: CharSequence? = null
    private var icon: Drawable? = null

    private var onMenuItemClickListener: MenuItem.OnMenuItemClickListener? = null

    override fun expandActionView(): Boolean {
        return false
    }

    override fun hasSubMenu(): Boolean = false

    override fun getMenuInfo(): ContextMenu.ContextMenuInfo? {
        return null
    }

    override fun getItemId(): Int = id

    override fun getAlphabeticShortcut(): Char = Char.MIN_VALUE

    override fun setEnabled(enabled: Boolean): MenuItem {
        isEnabled = enabled
        return this
    }

    override fun setTitle(title: CharSequence?): MenuItem {
        this.title = title
        return this
    }

    override fun setTitle(title: Int): MenuItem {
        this.title = resources.getString(title)
        return this
    }

    override fun setChecked(checked: Boolean): MenuItem {
        isChecked = checked
        return this
    }

    override fun getActionView(): View? {
        return null
    }

    override fun getTitle(): CharSequence? = title

    override fun getOrder(): Int {
        return 0
    }

    override fun setOnActionExpandListener(listener: MenuItem.OnActionExpandListener?): MenuItem {
        return this
    }

    override fun getIntent(): Intent? = null

    override fun setVisible(visible: Boolean): MenuItem {
        isVisible = visible
        return this
    }

    override fun isEnabled(): Boolean = isEnabled

    override fun isCheckable(): Boolean = isCheckable

    override fun setShowAsAction(actionEnum: Int) {
    }

    override fun getGroupId(): Int {
        return 0
    }

    override fun setActionProvider(actionProvider: ActionProvider?): MenuItem {
        return this
    }

    override fun setTitleCondensed(title: CharSequence?): MenuItem {
        return this
    }

    override fun getNumericShortcut(): Char {
        return '0'
    }

    override fun isActionViewExpanded(): Boolean {
        return false
    }

    override fun collapseActionView(): Boolean {
        return false
    }

    override fun isVisible(): Boolean = isVisible

    override fun setNumericShortcut(numericChar: Char): MenuItem {
        return this
    }

    override fun setActionView(view: View?): MenuItem {
        return this
    }

    override fun setActionView(resId: Int): MenuItem {
        return this
    }

    override fun setAlphabeticShortcut(alphaChar: Char): MenuItem {
        return this
    }

    override fun setIcon(icon: Drawable?): MenuItem {
        this.icon = icon
        return this
    }

    override fun setIcon(iconRes: Int): MenuItem {
        this.icon = resources.getDrawable(iconRes, context.theme)
        return this
    }

    override fun isChecked(): Boolean = isChecked

    override fun setIntent(intent: Intent?): MenuItem {
        return this
    }

    override fun setShortcut(numericChar: Char, alphaChar: Char): MenuItem {
        return this
    }

    override fun getIcon(): Drawable? = icon

    override fun setShowAsActionFlags(actionEnum: Int): MenuItem {
        return this
    }

    override fun setOnMenuItemClickListener(menuItemClickListener: MenuItem.OnMenuItemClickListener?): MenuItem {
        onMenuItemClickListener = menuItemClickListener
        return this
    }

    override fun getActionProvider(): ActionProvider? {
        return null
    }

    override fun setCheckable(checkable: Boolean): MenuItem {
        isCheckable = checkable
        return this
    }

    override fun getSubMenu(): SubMenu? {
        return null
    }

    override fun getTitleCondensed(): CharSequence? {
        return null
    }
}