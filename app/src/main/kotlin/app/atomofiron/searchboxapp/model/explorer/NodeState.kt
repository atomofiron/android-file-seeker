package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.Job

data class NodeStateImpl(
    // fields are hidden from view
    val uniqueId: Int,
    val cachingJob: Job? = null,
    override val operation: NodeOperation = NodeOperation.None,
) : NodeState {

    val empty: Boolean = cachingJob == null && operation is NodeOperation.None

    override val isCaching: Boolean get() = cachingJob != null
    override val isDeleting: Boolean get() = operation is NodeOperation.Deleting
    override val isCopying: Boolean get() = operation is NodeOperation.Copying
    override val withOperation: Boolean get() = operation !is NodeOperation.None
    override val inProgress: Boolean get() = operation.inProgress

    override fun toString(): String = "NodeState(caching=${cachingJob != null}, operation=${operation.javaClass.simpleName})"
}

interface NodeState {
    val operation: NodeOperation?
    val withOperation: Boolean
    val isCaching: Boolean
    val isDeleting: Boolean
    val isCopying: Boolean
    val inProgress: Boolean
}