package app.atomofiron.searchboxapp.model.other

sealed interface UiMode {
    data object Light : UiMode
    data class Dark(val black: Boolean) : UiMode

    val isBlack: Boolean get() = this is Dark && black

    companion object {
        operator fun invoke(isDark: Boolean, isBlack: Boolean) = if (isDark) Dark(isBlack) else Light
    }
}
