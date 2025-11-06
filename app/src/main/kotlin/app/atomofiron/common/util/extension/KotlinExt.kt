package app.atomofiron.common.util.extension

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList
import kotlin.math.ceil

inline fun <T> T.ctx(action: T.() -> Unit) = action()

operator fun CoroutineDispatcher.invoke(parallelism: Int) = Dispatchers.IO.limitedParallelism(parallelism)

inline fun CoroutineScope.launchOnDefault(
    noinline block: suspend CoroutineScope.() -> Unit,
) = launch(Dispatchers.Default, block = block)

inline fun CoroutineScope.launchOnIO(
    noinline block: suspend CoroutineScope.() -> Unit,
) = launch(Dispatchers.IO, block = block)

inline fun CoroutineScope.launchOnMain(
    immediate: Boolean = false,
    noinline block: suspend CoroutineScope.() -> Unit,
) = launch(if (immediate) Dispatchers.Main.immediate else Dispatchers.Main, block = block)

inline operator fun CoroutineScope.invoke(
    noinline block: suspend CoroutineScope.() -> Unit,
) = launch(block = block)

suspend inline fun withMain(
    now: Boolean = false,
    noinline action: suspend CoroutineScope.() -> Unit,
) = withContext(if (now) Dispatchers.Main.immediate else Dispatchers.Main, action)

inline infix fun <T> Boolean.then(action: () -> T): T? {
    return if (this) action() else null
}

@Suppress("NOTHING_TO_INLINE")
inline infix fun <T> Boolean.then(value: T): T? {
    return value.takeIf { this }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Any?.unit() = Unit

fun Float.ceilToInt(): Int = ceil(this).toInt()

inline fun <reified T> Any.cast() = this as T

fun Int.pow(exp: Int): Int = toLong().pow(exp.toLong()).toInt()

fun Long.pow(exp: Int): Long = pow(exp.toLong())

fun Long.pow(exp: Long): Long {
    require(exp >= 0) { "Negative exponent not supported for Int" }
    var result = 1L
    var base = this
    var exponent = exp
    while (exponent > 0L) {
        if ((exponent and 1L) == 1L) result *= base
        base *= base
        exponent = exponent shr 1
    }
    return result
}

inline fun <reified T : O, O> Any.takeIf(): T? = this as? T

inline fun <T, C : Iterable<T?>> C.onEachNotNull(action: (T) -> Unit): C {
    return apply { for (element in this) action(element ?: continue) }
}

inline fun <T : Any, R : Any> Iterable<T?>.mapNullable(transform: (T) -> R): List<R?> {
    return ArrayList<R?>().apply {
        this@mapNullable.forEach { item ->
            add(item?.let { transform(it) })
        }
    }
}

fun <T> List<T>.copy(): List<T> = mutableCopy()

fun <T> List<T>.mutableCopy(): MutableList<T> = toMutableList()

fun <T> MutableList<T>.clear(from: Int, to: Int = size) {
    val fromIndex = from.coerceAtMost(size)
    val toIndex = to.coerceAtMost(size)
    if (fromIndex <= toIndex) subList(fromIndex, toIndex).clear()
}

fun <T> MutableList<T>.resizeWith(size: Int, with: T) {
    when {
        this.size < size -> repeat(size - this.size) { add(with) }
        this.size > size -> subList(size, this.size).clear()
    }
    fill(with)
}

inline fun <T> List<T>.indexOfFirst(fromIndex: Int = 0, orElse: Int = -1, predicate: (T) -> Boolean): Int {
    if (fromIndex in indices) {
        var index = fromIndex
        for (item in listIterator(fromIndex)) {
            if (predicate(item)) {
                return index
            }
            index++
        }
    }
    return orElse
}

inline fun <T> MutableList<T>.set(new: T?, predicate: (T) -> Boolean): MutableList<T> {
    val iterator = listIterator()
    while (iterator.hasNext()) {
        val next = iterator.next()
        when {
            !predicate(next) -> continue
            new == null -> iterator.remove()
            else -> iterator.set(new)
        }
        return this
    }
    new?.let { add(new) }
    return this
}

inline fun <T> MutableList<T>.replace(action: (T) -> T?) {
    val iterator = listIterator()
    while (iterator.hasNext()) {
        val next = iterator.next()
        val new = action(next)
        when {
            new === next -> Unit
            new == null -> iterator.remove()
            else -> iterator.set(new)
        }
    }
}
