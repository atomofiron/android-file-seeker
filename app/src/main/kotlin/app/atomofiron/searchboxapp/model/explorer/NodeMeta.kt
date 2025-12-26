package app.atomofiron.searchboxapp.model.explorer

import kotlinx.serialization.Serializable

@Serializable
data class NodeMeta(
    override val access: String = "",
    override val owner: String = "",
    override val group: String = "",
    override val size: String = "",
    override val date: String = "",
    override val time: String = "",
    override val length: Long = -1,
) : NodeMetaData {
    companion object {
        const val DATE_TIME_SEPARATOR = " "
        const val DATE_TIME_FORMAT = "yyyy-MM-dd${DATE_TIME_SEPARATOR}HH:mm"

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
    val length: Long
}