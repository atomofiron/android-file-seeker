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
    override val withOperation: Boolean = operation !is Operation.None

    override fun toString(): String = "NodeState{caching=${cachingJob != null},operation=${operation.javaClass.simpleName}}"
}

sealed class Operation {
    object None : Operation()
    object Deleting : Operation()
    data class Copying(
        val isSource: Boolean,
        val asMoving: Boolean,
    ) : Operation()
    object Installing : Operation()
}

interface INodeState {
    val operation: Operation?
    val withOperation: Boolean
}