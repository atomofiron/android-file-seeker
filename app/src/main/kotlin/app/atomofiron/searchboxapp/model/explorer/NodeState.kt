package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.Job

data class NodeStateImpl(
    // fields are hidden from ui
    val uniqueId: NodeId,
    val cachingJob: Job? = null,
    override val operation: NodeOperation? = null,
) : NodeState {

    val isEmpty: Boolean = cachingJob == null && operation == null

    override val isCaching: Boolean get() = cachingJob != null
    override val isDeleting: Boolean get() = operation is NodeOperation.Deleting
    override val isCopying: Boolean get() = operation is NodeOperation.Copying
    override val withOperation: Boolean get() = operation != null
    override val isBusy: Boolean get() = operation?.busy == true

    override fun toString(): String = "NodeState(caching=${cachingJob != null}, operation=${operation?.javaClass?.simpleName})"
}

interface NodeState {
    val operation: NodeOperation?
    val withOperation: Boolean
    val isCaching: Boolean
    val isDeleting: Boolean
    val isCopying: Boolean
    val isBusy: Boolean
}