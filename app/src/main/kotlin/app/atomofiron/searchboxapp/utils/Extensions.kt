package app.atomofiron.searchboxapp.utils

import android.app.PendingIntent
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.FastScroller2
import app.atomofiron.searchboxapp.custom.FastScroller2.Action
import app.atomofiron.searchboxapp.custom.drawable.NoticeableDrawable
import app.atomofiron.searchboxapp.custom.view.dock.DockBarView
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import java.io.InputStream
import java.io.OutputStream

const val K = 1024
private val KUL = 1024.toULong()

fun String.escapeQuotes(): String = this.replace(Const.QUOTE, "\\" + Const.QUOTE)

inline fun InputStream.writeTo(out: OutputStream, callback: (Long) -> Unit = {}): Long {
    val buffer = ByteArray(16 * K)
    var copied: Long = 0
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        copied += bytes.toLong()
        callback(bytes.toLong())
        bytes = read(buffer)
    }
    return copied
}

fun Long.convert(
    suffixes: Array<String>,
    lossless: Boolean = true,
    separator: String = "",
): String = toULong().convert(suffixes, lossless, separator)

fun ULong.convert(
    suffixes: Array<String>,
    lossless: Boolean = true,
    separator: String = "",
): String {
    var value = this
    for (i in suffixes.indices) {
        if (value / KUL == ULong.MIN_VALUE) return "$value$separator${suffixes[i]}"
        if (lossless && value % KUL != ULong.MIN_VALUE) return "$value${suffixes[i]}"
        if (i < suffixes.lastIndex) value /= KUL
    }
    return "$value$separator${suffixes.last()}"
}

fun String.convertOrNull(): ULong? {
    val digits = Regex("\\d+")
    val metrics = Regex("([gGгГ]|[mMмМ]|[kKкК])?[bBбБ]?$")
    val value = digits.find(this)
        ?.value
        ?.toULongOrNull()
        ?: return null
    val rate = metrics.find(this)
        ?.value
        ?.takeIf { it.isNotEmpty() }
        ?: return value
    return when (rate.firstOrNull()) {
        'g', 'G', 'г', 'Г' -> K * K * K
        'm', 'M', 'м', 'М' -> K * K
        'k', 'K', 'к', 'К' -> K
        else -> 1
    }.toULong()
        .takeIf { value <= ULong.MAX_VALUE / it }
        ?.let { value * it }
}

fun Int.immutable(): Int = this or PendingIntent.FLAG_IMMUTABLE

inline fun <E> Iterable<E>.findWithIndexOrNull(predicate: (E) -> Boolean): Pair<Int, E?>? {
    return findWithIndex(predicate).takeIf { it.second != null }
}

inline fun <E> Iterable<E>.findWithIndex(predicate: (E) -> Boolean): Pair<Int, E?> {
    for ((index, item) in withIndex()) {
        if (predicate(item)) return index to item
    }
    return -1 to null
}

operator fun DockBarView.set(id: DockItem.Id, value: Boolean) {
    for (item in items) {
        if (item.id != id) continue
        val drawable = (item.icon as? DockItem.Icon.Value)?.drawable as? NoticeableDrawable
        drawable ?: continue
        drawable.forceShowDot(value)
    }
}

fun Boolean.toInt(): Int = if (this) 1 else -1

fun <E> MutableList<E>.removeOneIf(predicate: (E) -> Boolean): E? {
    val each = listIterator()
    while (each.hasNext()) {
        val item = each.next()
        if (predicate(item)) {
            each.remove()
            return item
        }
    }
    return null
}

inline fun <T> List<T>.mutate(action: MutableList<T>.() -> Unit): MutableList<T> {
    return toMutableList().apply(action)
}

inline fun <I> MutableList<I>.replaceEach(action: (I) -> I) {
    for (i in indices) {
        set(i, action(get(i)))
    }
}

inline fun <T> MutableList<T>.replaceOne(new: T, predicate: T.() -> Boolean) {
    val each = listIterator()
    while (each.hasNext()) {
        val item = each.next()
        if (predicate(item)) {
            each.set(new)
            return
        }
    }
}

fun <T> MutableList<T>.replaceAll(items: List<T>) {
    clear()
    addAll(items)
}

inline fun <reified E : I, I> MutableList<I>.replaceOne(action: E.() -> E): E? {
    for (i in indices) {
        val element = get(i)
        if (element is E) {
            val new = action(element)
            set(i, action(element))
            return new
        }
    }
    return null
}

inline fun <T,R> Iterable<T>.findNotNull(predicate: (T) -> R?): R {
    for (element in this) {
        val value = predicate(element)
        if (value != null) return value
    }
    throw IllegalStateException(toString())
}

fun <T> MutableList<T>.move(from: Int, to: Int) {
    val step = when {
        from == to -> return
        from < to -> 1
        else -> -1
    }
    val element = get(from)
    var free = from
    while (free != to) {
        set(free, get(free + step))
        free += step
    }
    set(to, element)
}

inline fun <T, R : Comparable<R>> MutableList<T>.sortBy(descending: Boolean = false, crossinline selector: (T) -> R?) {
    return if (descending) sortByDescending(selector) else sortBy(selector)
}

// prevents ConcurrentModificationException
inline fun <reified E> List<E>.findOnMut(predicate: (E) -> Boolean): E? {
    var size = size
    var index = 0
    while (index < size) {
        val item = getOrNull(index)
        if (size != this.size) {
            size = this.size
            index = 0
            continue
        } else if (item is E && predicate(item)) {
            return item
        }
        index++
    }
    return null
}

fun RecyclerView.addFastScroll(inTheEnd: Boolean, callback: ((Action) -> Unit)? = null) = FastScroller2(
    this,
    ContextCompat.getDrawable(context, R.drawable.scroll_thumb) as StateListDrawable,
    ContextCompat.getDrawable(context, R.drawable.scroll_track) as Drawable,
    ContextCompat.getDrawable(context, R.drawable.scroll_thumb) as StateListDrawable,
    ContextCompat.getDrawable(context, R.drawable.scroll_track) as Drawable,
    thickness = resources.getDimensionPixelSize(R.dimen.fastscroll_thickness),
    mScrollbarMinimumRange = resources.getDimensionPixelSize(R.dimen.fastscroll_minimum_range),
    minDragAreaSize = resources.getDimensionPixelSize(R.dimen.fastscroll_area),
    minThumbLength = resources.getDimensionPixelSize(R.dimen.fastscroll_minimum_size),
    inTheEnd = inTheEnd,
    callback = callback,
)
