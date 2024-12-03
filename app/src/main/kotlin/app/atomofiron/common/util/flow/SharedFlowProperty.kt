package app.atomofiron.common.util.flow

import kotlinx.coroutines.flow.SharedFlow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class SharedFlowProperty<T>(private val flow: SharedFlow<T>) : SharedFlow<T> by flow, ReadOnlyProperty<Any?, T> {

    val value: T get() = flow.replayCache.first()

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}

fun <T> SharedFlow<T>.asProperty(): SharedFlowProperty<T> = SharedFlowProperty(this)