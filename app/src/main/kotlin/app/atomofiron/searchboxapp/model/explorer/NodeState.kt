package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.Job

data class NodeState(
    // fields hidden from view
    val uniqueId: Int,
    val cachingJob: Job? = null,
    override val operation: Operation = Operation.None,
) : INodeState {
    val withoutState: Boolean = cachingJob == null && operation is Operation.None

    val isCaching: Boolean = cachingJob != null
    val isDeleting: Boolean = operation is Operation.Deleting
    val isCopying: Boolean = operation is Operation.Copying
    override val withOperation: Boolean = operation !is Operation.None

    override fun toString(): String = "NodeState{caching=${cachingJob != null},operation=${operation.javaClass.simpleName}}"
}

sealed class Operation {
    data object None : Operation()
    data object Deleting : Operation()
    data class Copying(
        val isSource: Boolean,
        val asMoving: Boolean = false,
    ) : Operation()
    data object Installing : Operation()
}

interface INodeState {
    val operation: Operation?
    val withOperation: Boolean
}