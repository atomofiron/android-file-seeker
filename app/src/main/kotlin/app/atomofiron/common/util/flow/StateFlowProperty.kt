package app.atomofiron.common.util.flow

import app.atomofiron.common.util.property.RoProperty
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KProperty

class StateFlowProperty<T>(private val flow: SharedFlow<T>) : SharedFlow<T> by flow
    , StateFlow<T>
    , RoProperty<T>
{
    override val value: T get() = flow.replayCache.first()
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}

fun <T> SharedFlow<T>.asProperty(): StateFlowProperty<T> = StateFlowProperty(this)