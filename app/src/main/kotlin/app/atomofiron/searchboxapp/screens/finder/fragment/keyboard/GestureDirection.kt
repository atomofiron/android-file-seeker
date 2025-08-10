package app.atomofiron.searchboxapp.screens.finder.fragment.keyboard;

enum class GestureDirection(val right: Boolean) {
    Right(true),
    Left(false),
    ;
    companion object Companion {
        fun right(right: Boolean) = if (right) Right else Left
    }
}
