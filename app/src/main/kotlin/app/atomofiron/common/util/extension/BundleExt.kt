package app.atomofiron.common.util.extension

import android.os.Bundle
import androidx.work.Data

inline fun <reified T : Any> protobufBytesKey(): String = T::class.java.name

inline fun <reified T : Any> Bundle.get(): T? = getByteArray(protobufBytesKey<T>())?.decode()

inline fun <reified T : Any> Bundle.put(data: T): Bundle {
    putByteArray(protobufBytesKey<T>(), data.encode())
    return this
}

inline operator fun <reified T : Any> Data.Companion.invoke(data: T): Data {
    return Data.Builder().put(this).build()
}

inline fun <reified T : Any> Data.get(): T? = getByteArray(protobufBytesKey<T>())?.decode()

inline fun <reified T : Any> Data.Builder.put(data: T): Data.Builder {
    putByteArray(protobufBytesKey<T>(), data.encode())
    return this
}
