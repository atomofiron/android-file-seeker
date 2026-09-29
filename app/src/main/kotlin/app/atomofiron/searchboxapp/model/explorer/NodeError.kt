package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.debugRequire
import kotlinx.serialization.Serializable

@Serializable
sealed class NodeError {

    @Serializable
    data object NoSuchFileOrDir : NodeError()

    @Serializable
    data object FileWasChanged : NodeError()

    @Serializable
    data object PermissionDenied : NodeError()

    @Serializable
    data object ResourceBusy : NodeError()

    @Serializable
    data object Unknown : NodeError()

    @Serializable
    data class Multiply(val lines: List<String>) : NodeError()

    @Serializable
    data class Message(val message: String) : NodeError() {
        companion object {
            fun orUnknown(message: String?) = when {
                message.isNullOrBlank() -> Unknown
                else -> Message(message)
            }
        }
        init {
            debugRequire(message.isNotBlank()) { "error message is empty" }
        }
    }

    override fun toString(): String {
        val message = (this as? Message)?.message?.let { "($it)" } ?: ""
        return javaClass.simpleName + message
    }
}