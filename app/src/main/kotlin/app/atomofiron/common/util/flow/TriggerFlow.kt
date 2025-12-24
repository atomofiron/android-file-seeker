package app.atomofiron.common.util.flow

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
class TriggerFlow<T> private constructor(
    private var initial: T? = null,
    private val impl: MutableSharedFlow<T>,
) : MutableSharedFlow<T> by impl {

    constructor(initial: T? = null) : this(initial, MutableSharedFlow<T>(replay = 1, 1, DROP_OLDEST))

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        if (initial != null && replayCache.isEmpty()) {
            initial?.let { collector.emit(it) }
            initial = null
        }
        impl.collect(collector)
    }
}