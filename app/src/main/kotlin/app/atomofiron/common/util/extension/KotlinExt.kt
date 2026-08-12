package app.atomofiron.common.util.extension

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.util.UUID
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
    start: CoroutineStart = CoroutineStart.DEFAULT,
    noinline block: suspend CoroutineScope.() -> Unit,
) = launch(if (immediate) Dispatchers.Main.immediate else Dispatchers.Main, start, block = block)

inline operator fun CoroutineScope.invoke(
    noinline block: suspend CoroutineScope.() -> Unit,
) = launch(block = block)

suspend inline fun withMain(
    now: Boolean = false,
    noinline action: suspend CoroutineScope.() -> Unit,
) = withContext(if (now) Dispatchers.Main.immediate else Dispatchers.Main, action)

suspend inline fun withIO(
    noinline action: suspend CoroutineScope.() -> Unit,
) = withContext(Dispatchers.IO, action)

inline infix fun <T> Boolean.then(action: () -> T): T? {
    return if (this) action() else null
}

@Suppress("NOTHING_TO_INLINE")
inline infix fun <T> Boolean.then(value: T): T? {
    return value.takeIf { this }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Any?.unit() = Unit

@Suppress("NOTHING_TO_INLINE")
inline fun Any?.nil() = null

fun Float.ceilToInt(): Int = ceil(this).toInt()

inline fun <reified T> Any.cast() = this as T

inline fun <reified T> Any?.tryAs() = this as? T

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

inline fun <T, reified S : T, R> List<T>.mapCast(transform: S.() -> R?): List<R> {
    return mapNotNull {
        when (it) {
            is S -> it.transform()
            else -> null
        }
    }
}

fun <T> List<T>.takeIfNotEmpty(): List<T>? = takeIf { it.isNotEmpty() }

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

inline fun <T> MutableList<T>.put(new: T?, predicate: (T) -> Boolean): MutableList<T> {
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

fun <T> MutableList<T>.setAt(index: Int, new: T?) = when {
    index > size -> Unit
    new != null -> set(index, new)
    index == size -> Unit
    else -> removeAt(index)
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

inline fun <reified R> Sequence<*>.findAs(): R? = find { it is R } as R?

val IntRange.size: Int get() = if (isEmpty()) 0 else last - first + 1

fun Int.hasBits(bits: Int) = (this and bits) == bits

fun UUID.toBytes(): ByteArray {
    val buffer = ByteBuffer.allocate(16)
    buffer.putLong(mostSignificantBits)
    buffer.putLong(leastSignificantBits)
    return buffer.array()
}

fun ByteArray.toUUID(): UUID {
    require(size == 16) { "ByteArray[$size].toUUID()" }
    val buffer = ByteBuffer.wrap(this)
    return UUID(buffer.long, buffer.long)
}

fun StringBuilder.appendWithComma(part: String): StringBuilder {
    if (isNotBlank()) append(", ")
    append(part)
    return this
}

fun hash(a: Any?) = when (a) {
    null -> 0
    is ByteArray -> a.contentHashCode()
    is ShortArray -> a.contentHashCode()
    is IntArray -> a.contentHashCode()
    is LongArray -> a.contentHashCode()
    is CharArray -> a.contentHashCode()
    is FloatArray -> a.contentHashCode()
    is DoubleArray -> a.contentHashCode()
    is BooleanArray -> a.contentHashCode()
    is Array<*> -> a.contentDeepHashCode()
    else -> a.hashCode()
}

fun hash(a: Any?, b: Any?): Int = hash(hash(a), hash(b))

fun hash(a: Any?, b: Any?, c: Any?): Int = hash(hash(a), hash(b), hash(c))

fun hash(a: Any?, b: Any?, c: Any?, d: Any?): Int = hash(hash(a), hash(b), hash(c), hash(d))

fun hash(a: Any?, b: Any?, c: Any?, d: Any?, e: Any?): Int = hash(hash(a), hash(b), hash(c), hash(d), hash(e))

private fun hash(a: Int, b: Int, c: Int = 0, d: Int = 0, e: Int = 0): Int {
    var result = a
    // it's bad for hash(0), but it's not so bad for all I think
    if (b != 0) result = result.append(b)
    if (c != 0) result = result.append(c)
    if (d != 0) result = result.append(c)
    if (e != 0) result = result.append(e)
    return result
}

private fun Int.append(other: Int) = 31 * this + other
