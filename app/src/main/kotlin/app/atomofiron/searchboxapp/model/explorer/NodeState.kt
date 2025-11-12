package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.Job

data class NodeStateImpl(
    // fields are hidden from view
    val uniqueId: Int,
    val cachingJob: Job? = null,
    override val operation: NodeOperation = NodeOperation.None,
) : NodeState {
    val empty: Boolean = cachingJob == null && operation is NodeOperation.None

    override val isCaching: Boolean = cachingJob != null
    override val isDeleting: Boolean = operation is NodeOperation.Deleting
    override val isCopying: Boolean = operation is NodeOperation.Copying
    override val withOperation: Boolean = operation !is NodeOperation.None

    override fun toString(): String = "NodeState{caching=${cachingJob != null},operation=${operation.javaClass.simpleName}}"
}

interface NodeState {
    val operation: NodeOperation?
    val withOperation: Boolean
    val isCaching: Boolean
    val isDeleting: Boolean
    val isCopying: Boolean
}