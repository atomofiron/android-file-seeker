package app.atomofiron.common.util.flow

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
class TriggerFlow private constructor(private val impl: MutableSharedFlow<Unit>) : MutableSharedFlow<Unit> by impl {

    constructor() : this(MutableSharedFlow<Unit>())

    override suspend fun collect(collector: FlowCollector<Unit>): Nothing {
        if (replayCache.isEmpty()) collector.emit(Unit)
        impl.collect(collector)
    }
}