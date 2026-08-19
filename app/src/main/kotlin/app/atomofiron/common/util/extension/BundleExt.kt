package app.atomofiron.common.util.extension

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import androidx.work.Data
import kotlin.uuid.Uuid

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

fun Bundle.put(uuid: Uuid): Bundle {
    putByteArray(Uuid::class.java.simpleName, uuid.toByteArray())
    return this
}

fun Bundle.getUuid(): Uuid? = getByteArray(Uuid::class.java.simpleName)
    ?.let { Uuid.fromByteArray(it) }

fun Bundle?.calcSize(): Int? {
    val parcel = Parcel.obtain()
    return try {
        parcel.writeBundle(this)
        parcel.dataSize()
    } catch (_: Exception) {
        null
    } finally {
        parcel.recycle()
    }
}
