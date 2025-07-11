package app.atomofiron.searchboxapp.model.explorer

data class NodeProperties(
    override val access: String = "",
    override val owner: String = "",
    override val group: String = "",
    override val size: String = "",
    override val date: String = "",
    override val time: String = "",
    override val name: String = "",
    override val length: Long = -1,
) : INodeProperties {
    companion object {
        const val DATE_TIME_SEPARATOR = " "
        const val DATE_TIME_FORMAT = "yyyy-MM-dd${DATE_TIME_SEPARATOR}HH:mm"
    }
}

// -rw-r-----  1 root everybody   5348187 2019-06-13 18:19 Magisk-v19.3.zip
interface INodeProperties {
    val access: String
    val owner: String
    val group: String
    val size: String
    val date: String
    val time: String
    val name: String
    val length: Long
}