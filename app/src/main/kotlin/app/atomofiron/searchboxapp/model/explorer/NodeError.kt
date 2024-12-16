package app.atomofiron.searchboxapp.model.explorer

sealed class NodeError {
    data object NoSuchFile : NodeError()
    data object PermissionDenied : NodeError()
    data object Unknown : NodeError()
    data class Multiply(val lines: List<String>) : NodeError()
    data class Message(val message: String) : NodeError()

    override fun toString(): String {
        val message = (this as? Message)?.message?.let { "($it)" } ?: ""
        return javaClass.simpleName + message
    }
}