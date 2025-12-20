package app.atomofiron.searchboxapp.screens.viewer.state

import app.atomofiron.searchboxapp.utils.toInt

data class Status(
    val loading: Boolean = false,
    val current: Int = 0,
    val max: Int = 0,
) {
    fun clear(): Status = copy(current = 0, max = 0)
    fun go(forward: Boolean): Status = copy(current = ((max + current.dec() + forward.toInt()) % max).inc())
}
