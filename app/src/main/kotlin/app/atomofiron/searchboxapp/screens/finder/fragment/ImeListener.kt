package app.atomofiron.searchboxapp.screens.finder.fragment

interface ImeListener {
    fun onImeStart(max: Int)
    fun onImeMove(inset: Int)
    fun onImeEnd()
}