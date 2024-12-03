package app.atomofiron.common.util.flow

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KProperty

class MutableDataFlowProperty<T>(value: T) : MutableStateFlow<T> by MutableStateFlow(value) {
    operator fun getValue(any: Any, property: KProperty<*>): T = value
    operator fun setValue(any: Any, property: KProperty<*>, value: T) = set(value)
}