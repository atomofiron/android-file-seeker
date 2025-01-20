package app.atomofiron.searchboxapp.model.network

sealed interface Loading {
    data class Progress(val done: Long, val length: Long) : Loading {
        companion object {
            val Indeterminate = Progress(0, 0)
        }
        val progress: Float? get() = if (length == 0L) null else (done.toFloat() / length)
    }
    data object Completed : Loading
    data class Error(val cause: String) : Loading
}