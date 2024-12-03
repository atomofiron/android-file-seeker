package app.atomofiron.searchboxapp.custom.view.menu

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.view.menu.MenuBuilder

@SuppressLint("RestrictedApi")
class MenuImpl(context: Context) : MenuBuilder(context) {

    private var menuChangedListener: (() -> Unit)? = null

    override fun onItemsChanged(structureChanged: Boolean) {
        super.onItemsChanged(structureChanged)
        menuChangedListener?.invoke()
    }

    fun setMenuChangedListener(listener: () -> Unit) {
        menuChangedListener = listener
    }
}