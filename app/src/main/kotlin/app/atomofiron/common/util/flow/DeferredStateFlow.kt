package app.atomofiron.common.util.flow

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DeferredStateFlow<T>(value: T? = null) : StateFlow<T> {

    private val flow = MutableStateFlow(Nullable(value))

    override val replayCache: List<T> = flow.replayCache.mapNotNull { it.value }

    override var value: T
        get() = flow.value.value!!
        set(value) {
            flow.value = Nullable(value)
        }

    val valueOrNull: T? get() = flow.value.value

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        flow.collect { nullable ->
            nullable.value?.let {
                collector.emit(it)
            }
        }
    }

    private data class Nullable<T>(val value: T?)
}