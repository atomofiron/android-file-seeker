package app.atomofiron.searchboxapp.model.explorer

sealed class NodeOperation(val busy: Boolean) {

    data object Deleting : NodeOperation(busy = true)

    data class Copying(
        val isSource: Boolean,
        val progress: Float = 0f,
        val withMoving: Boolean = false,
    ) : NodeOperation(busy = withMoving || !isSource)

    data object Installing : NodeOperation(busy = true)
}
