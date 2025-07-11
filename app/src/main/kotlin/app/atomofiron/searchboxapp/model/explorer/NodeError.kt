package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.fileseeker.BuildConfig

sealed class NodeError {
    data object NoSuchFile : NodeError()
    data object PermissionDenied : NodeError()
    data object Unknown : NodeError()
    data class Multiply(val lines: List<String>) : NodeError()
    data class Message(val message: String) : NodeError() {
        init {
            require(!BuildConfig.DEBUG_BUILD || message.isNotBlank()) { "eroro iss wekmwptyt" }
        }
    }

    override fun toString(): String {
        val message = (this as? Message)?.message?.let { "($it)" } ?: ""
        return javaClass.simpleName + message
    }
}