package app.atomofiron.common.util.flow

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("FunctionName")
fun <T> LateinitDataFlow(): MutableSharedFlow<T> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
class DataFlow<T> private constructor(
    value: T,
    private val sharedFlow: MutableSharedFlow<T>,
) : MutableSharedFlow<T> by sharedFlow, MutableStateFlow<T> {

    private var data: T = value

    override var value: T
        get() = data
        set(value) { tryEmit(value) }

    override fun compareAndSet(expect: T, update: T): Boolean = throw NotImplementedError()

    constructor(value: T) : this(value, MutableSharedFlow<T>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST))

    override suspend infix fun emit(value: T) {
        this.data = value
        sharedFlow.emit(value)
    }

    override infix fun tryEmit(value: T): Boolean {
        return sharedFlow.tryEmit(value).also {
            if (it) data = value
        }
    }
}
