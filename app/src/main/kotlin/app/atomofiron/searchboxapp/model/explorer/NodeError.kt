package app.atomofiron.searchboxapp.model.explorer

sealed class NodeError {
    object NoSuchFile : NodeError()
    object PermissionDenied : NodeError()
    object Unknown : NodeError()
    class Multiply(val lines: List<String>) : NodeError()
    class Message(val message: String) : NodeError()

    override fun toString(): String {
        val message = (this as? Message)?.message?.let { "($it)" } ?: ""
        return javaClass.simpleName + message
    }
}