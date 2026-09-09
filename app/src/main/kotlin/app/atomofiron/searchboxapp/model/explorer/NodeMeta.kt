package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.searchboxapp.utils.Const.UNDEFINED_FILE_LENGTH
import app.atomofiron.searchboxapp.utils.Const.UNDEFINED_FILE_TIMESTAMP
import kotlinx.serialization.Serializable

@Serializable
data class NodeMeta(
    override val access: String = "",
    override val owner: String = "",
    override val group: String = "",
    override val size: String = "",
    override val date: String = "",
    override val time: String = "",
    override val length: ULong = UNDEFINED_FILE_LENGTH,
    override val timestamp: Long = UNDEFINED_FILE_TIMESTAMP,
) : NodeMetaData {
    companion object {
        val Empty = NodeMeta()
    }
}

interface NodeMetaData {
    val access: String
    val owner: String
    val group: String
    val size: String
    val date: String
    val time: String
    val length: ULong
    val timestamp: Long
}