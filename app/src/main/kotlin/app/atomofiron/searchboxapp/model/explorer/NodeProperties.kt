package app.atomofiron.searchboxapp.model.explorer

data class NodeProperties(
    override val access: String = "",
    override val owner: String = "",
    override val group: String = "",
    override val size: String = "",
    override val date: String = "",
    override val time: String = "",
    override val name: String = "",
) : INodeProperties

// -rw-r-----  1 root everybody   5348187 2019-06-13 18:19 Magisk-v19.3.zip
interface INodeProperties {
    val access: String
    val owner: String
    val group: String
    val size: String
    val date: String
    val time: String
    val name: String
}