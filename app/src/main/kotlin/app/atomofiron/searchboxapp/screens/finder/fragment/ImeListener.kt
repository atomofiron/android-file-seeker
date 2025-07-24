package app.atomofiron.searchboxapp.screens.finder.fragment

fun interface ImeListener {
    operator fun invoke(bottom: Int, end: Boolean)
}