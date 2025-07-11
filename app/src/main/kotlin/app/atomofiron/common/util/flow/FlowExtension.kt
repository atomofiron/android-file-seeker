package app.atomofiron.common.util.flow

import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

val <T> SharedFlow<T>.value: T get() = replayCache.last()

val <T> SharedFlow<T>.valueOrNull: T? get() = replayCache.lastOrNull()

operator fun MutableSharedFlow<Unit>.invoke(scope: CoroutineScope) {
    this[scope] = Unit
}

operator fun <T> MutableSharedFlow<T>.set(scope: CoroutineScope, value: T) {
    scope.launch { emit(value) }
}

fun <T> MutableStateFlow<T>.emitLast() {
    value = value
}

fun <T> MutableStateFlow<T>.set(value: T) {
    this.value = value
}

operator fun MutableStateFlow<Unit>.invoke() = set(Unit)

fun <T> Flow<T>.collect(scope: CoroutineScope, collector: FlowCollector<T>) {
    scope.launch { collect(collector) }
}

fun <T> Flow<T>.collect(
    scope: CoroutineScope,
    context: CoroutineContext,
    collector: FlowCollector<T>,
) {
    scope.launch(context) { collect(collector) }
}

fun <T> Flow<T>.launch(scope: CoroutineScope, context: CoroutineContext = EmptyCoroutineContext) {
    scope.launch(context) { collect { } }
}

fun <T> Fragment.viewCollect(
    flow: Flow<T>,
    immediate: Boolean = true,
    collector: FlowCollector<T>,
) {
    val context = when {
        immediate -> Dispatchers.Main.immediate
        else -> Dispatchers.Main
    }
    viewLifecycleOwner.lifecycle.coroutineScope.launch(context) {
        flow.collect {
            collector.emit(it)
        }
    }
}

fun <T> Fragment.fragmentCollect(
    flow: Flow<T>,
    immediate: Boolean = true,
    collector: FlowCollector<T>,
) {
    val context = when {
        immediate -> Dispatchers.Main.immediate
        else -> Dispatchers.Main
    }
    lifecycle.coroutineScope.launch(context) {
        flow.collect(collector)
    }
}

fun <T> Flow<T>.throttleLatest(duration: Long): Flow<T> = conflate().transform {
    emit(it)
    delay(duration)
}

fun <T,R> StateFlow<T>.transform(transform: (T) -> R): StateFlow<R> {
    val original = this
    return object : StateFlow<R>, Flow<R> by original.map(transform) {
        override val value: R get() = transform(original.value)
        override val replayCache: List<R>
            //get() = original.replayCache.map(transform)
            get() = throw NotImplementedError()

        override suspend fun collect(collector: FlowCollector<R>): Nothing {
            original.collect {
                collector.emit(transform(it))
            }
        }
    }
}
