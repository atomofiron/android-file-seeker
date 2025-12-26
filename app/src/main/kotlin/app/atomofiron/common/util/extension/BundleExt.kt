package app.atomofiron.common.util.extension

import android.content.Intent
import android.os.Bundle
import androidx.work.Data
import java.util.UUID

inline fun <reified T : Any> protobufBytesKey(): String = T::class.java.name

inline fun <reified T : Any> Bundle.get(): T? = getByteArray(protobufBytesKey<T>())?.decodeOrNull()

inline fun <reified T : Any> Bundle.put(data: T): Bundle {
    putByteArray(protobufBytesKey<T>(), data.encode())
    return this
}

inline fun <reified T : Any> Intent.get(): T? = getByteArrayExtra(protobufBytesKey<T>())?.decodeOrNull()

inline fun <reified T : Any> Intent.put(data: T): Intent {
    putExtra(protobufBytesKey<T>(), data.encode())
    return this
}

inline operator fun <reified T : Any> Data.Companion.invoke(data: T): Data {
    return Data.Builder().put(data).build()
}

inline fun <reified T : Any> Data.get(): T? = getByteArray(protobufBytesKey<T>())?.decodeOrNull()

inline fun <reified T : Any> Data.Builder.put(data: T): Data.Builder {
    putByteArray(protobufBytesKey<T>(), data.encode())
    return this
}

fun Bundle.put(uuid: UUID): Bundle {
    putByteArray(UUID::class.java.simpleName, uuid.toBytes())
    return this
}

fun Bundle.getUUID(): UUID? = getByteArray(UUID::class.java.simpleName)?.toUUID()
