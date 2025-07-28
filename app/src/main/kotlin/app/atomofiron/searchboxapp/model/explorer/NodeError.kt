package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.debugRequire

sealed class NodeError {
    data object NoSuchFile : NodeError()
    data object PermissionDenied : NodeError()
    data object Unknown : NodeError()
    data class Multiply(val lines: List<String>) : NodeError()
    data class Message(val message: String) : NodeError() {
        init { debugRequire(message.isNotBlank()) { "error message is empty" } }
    }

    override fun toString(): String {
        val message = (this as? Message)?.message?.let { "($it)" } ?: ""
        return javaClass.simpleName + message
    }
}