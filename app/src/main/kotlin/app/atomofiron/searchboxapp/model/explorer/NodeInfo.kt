package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.hash
import kotlinx.serialization.Serializable

@Serializable
data class NodeInfo(
    val ref: NodeRef,
    val hash: NodeHash,
    val mime: String,
    val meta: NodeMeta,
) {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is NodeInfo -> false
        ref != other.ref -> false
        mime != other.mime -> false
        else -> hash != other.hash
    }

    override fun hashCode(): Int = hash(ref, mime, hash, meta)

    override fun toString() = "NodeHash(ref=$ref, hash=${hash.toHexString()}, mime=$mime, meta=$meta)"
}
