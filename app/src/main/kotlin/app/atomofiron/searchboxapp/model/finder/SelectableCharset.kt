package app.atomofiron.searchboxapp.model.finder

data class SelectableCharset(
    val name: String,
    val selected: Boolean = false,
    val enabled: Boolean = true,
)