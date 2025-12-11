package app.atomofiron.common.util.extension

import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

inline fun <reified T : Any> T.encode() = ProtoBuf.encodeToByteArray(this)

inline fun <reified T : Any> ByteArray.decode(): T? = when {
    isEmpty() -> null
    else -> ProtoBuf.decodeFromByteArray<T>(this)
}
