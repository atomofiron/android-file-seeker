package app.atomofiron.common.util.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.roundToInt

inline fun <T> T.ctx(action: T.() -> Unit) = action()

suspend fun withMain(
    now: Boolean = false,
    action: suspend CoroutineScope.() -> Unit,
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

fun Float.fix(): Float = (this * 10000).roundToInt() / 10000f

fun <T> List<T>.copy(): List<T> = mutableCopy()

fun <T> List<T>.mutableCopy(): MutableList<T> = toMutableList()

fun <T> MutableList<T>.clear(from: Int, to: Int = size) {
    val fromIndex = from.coerceAtMost(size)
    val toIndex = to.coerceAtMost(size)
    if (fromIndex <= toIndex) subList(fromIndex, toIndex).clear()
}

fun Int.rangePlus(over: Int): IntRange = this..<(this + over)

fun IntRange.relative(absolute: Int) = absolute - first
