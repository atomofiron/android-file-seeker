package app.atomofiron.searchboxapp.screens.finder.fragment

interface KeyboardInsetListener {
    fun onImeStart(max: Int) = Unit
    fun onImeMove(current: Int) = Unit
    fun onImeEnd(visible: Boolean) = Unit
}