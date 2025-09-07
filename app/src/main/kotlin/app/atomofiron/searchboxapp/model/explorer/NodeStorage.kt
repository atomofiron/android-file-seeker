package app.atomofiron.searchboxapp.model.explorer

data class NodeStorage(
    val kind: Kind,
    val path: String,
    val name: String?,
    val alias: String?,
    val total: Long = 0,
    val used: Long = 0,
) {
    enum class Kind(val removable: Boolean) {
        InternalStorage(false),
        UsbStorage(true),
        SdCard(true),
    }
}