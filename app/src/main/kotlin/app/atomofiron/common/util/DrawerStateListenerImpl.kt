package app.atomofiron.common.util

import android.view.View
import androidx.drawerlayout.widget.DrawerLayout

class DrawerStateListenerImpl : DrawerLayout.DrawerListener {
    private var state: Int = 0
    private var opened = false

    val isOpened: Boolean get() = opened || !opened && state == DrawerLayout.STATE_SETTLING

    override fun onDrawerStateChanged(newState: Int) {
        state = newState
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit

    override fun onDrawerClosed(drawerView: View) {
        opened = false
    }

    override fun onDrawerOpened(drawerView: View) {
        opened = true
    }
}