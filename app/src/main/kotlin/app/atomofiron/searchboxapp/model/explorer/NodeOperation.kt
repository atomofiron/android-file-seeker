package app.atomofiron.searchboxapp.model.explorer

sealed class NodeOperation {

    data object None : NodeOperation()

    data object Deleting : NodeOperation()

    data class Copying(
        val isSource: Boolean,
        val asMoving: Boolean = false,
    ) : NodeOperation()

    data object Installing : NodeOperation()
}
