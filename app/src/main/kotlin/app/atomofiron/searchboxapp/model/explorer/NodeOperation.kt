package app.atomofiron.searchboxapp.model.explorer

sealed class NodeOperation(val inProgress: Boolean) {

    data object None : NodeOperation(inProgress = false)

    data object Deleting : NodeOperation(inProgress = true)

    data class Copying(
        val isSource: Boolean,
        val progress: Float = 0f,
        val withMoving: Boolean = false,
    ) : NodeOperation(inProgress = withMoving || !isSource)

    data object Installing : NodeOperation(inProgress = true)
}
