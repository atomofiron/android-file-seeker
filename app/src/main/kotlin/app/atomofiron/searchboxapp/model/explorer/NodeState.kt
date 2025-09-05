package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.Job

data class NodeState(
    // fields hidden from view
    val uniqueId: Int,
    val cachingJob: Job? = null,
    override val operation: NodeOperation = NodeOperation.None,
) : INodeState {
    val withoutState: Boolean = cachingJob == null && operation is NodeOperation.None

    val isCaching: Boolean = cachingJob != null
    val isDeleting: Boolean = operation is NodeOperation.Deleting
    val isCopying: Boolean = operation is NodeOperation.Copying
    override val withOperation: Boolean = operation !is NodeOperation.None

    override fun toString(): String = "NodeState{caching=${cachingJob != null},operation=${operation.javaClass.simpleName}}"
}

interface INodeState {
    val operation: NodeOperation?
    val withOperation: Boolean
}