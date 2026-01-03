package app.atomofiron.common.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import app.atomofiron.fileseeker.R
import kotlin.math.min

fun Window.reallyDisableFitsSystemWindows() {
    WindowCompat.setDecorFitsSystemWindows(this, false)
    // WindowCompat.setDecorFitsSystemWindows() is not enough
    decorView.giveMeFuckingInsets(4)
}

private fun View.giveMeFuckingInsets(downCount: Int) {
    fitsSystemWindows = false
    if (downCount > 0) (this as? ViewGroup)?.let {
        for (i in 0..<childCount) {
            getChildAt(i).giveMeFuckingInsets(downCount.dec())
        }
    }
}

fun View.showKeyboard(): Boolean {
    requestFocus()
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    return inputMethodManager?.showSoftInput(this, 0) == true
}

fun View.hideKeyboard(): Boolean {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    val wasShown = inputMethodManager?.hideSoftInputFromWindow(windowToken, 0) == true
    clearFocus()
    return wasShown
}

fun Context.findBooleanByAttr(@AttrRes attr: Int): Boolean = findBooleansByAttr(attr)[0]

fun Context.findBooleansByAttr(@AttrRes vararg attrs: Int): BooleanArray {
    @SuppressLint("ResourceType")
    val array = obtainStyledAttributes(attrs)

    val values = BooleanArray(attrs.size)
    for (i in attrs.indices) {
        values[i] = array.getBoolean(i, false)
    }
    array.recycle()

    return values
}

fun ViewGroup.moveChildrenFrom(layoutId: Int) {
    val inflater = LayoutInflater.from(context)
    val container = inflater.inflate(layoutId, null, false) as ViewGroup
    for (i in 0 until container.childCount) {
        val child = container.getChildAt(0)
        container.removeView(child)
        addView(child)
    }
}

fun Context.isDarkTheme(): Boolean = findBooleanByAttr(R.attr.isDarkTheme)

fun Context.isBlackDeep(): Boolean = findBooleanByAttr(R.attr.isBlackDeep)

fun Context.isDarkDeep(): Boolean = isDarkTheme() && isBlackDeep()

fun Int.Companion.random(range: Int = 1000): Int = (Math.random() * range).toInt()

fun Boolean.Companion.random(probability: Double = 0.5): Boolean = Math.random() < probability

infix fun Int.progressionTo(other: Int) = when {
    this <= other -> this..other
    else -> this downTo other
}

fun <T : View> T.ifVisible(action: T.() -> Unit) {
    if (isVisible) action()
}

inline fun <T> T.applyIf(predicate: Boolean, action: T.() -> Unit) = when {
    predicate -> apply(action)
    else -> this
}

inline fun <T,Q : Any> T.withNotNull(it: Q?, action: T.(Q) -> Unit) = apply {
    it?.let { action(this, it) }
}

fun Throwable.forHumans() = "${this::class.simpleName}: $message"

fun ViewParent.noClip() = (this as? ViewGroup)?.noClip()

fun ViewGroup.noClip() {
    clipToPadding = false
    clipChildren = false
    clipToOutline = false
}

fun Float.coerceInRange(from: Float, to: Float) = when {
    isNaN() -> min(from, to)
    from > to -> when {
        this < to -> to
        this > from -> from
        else -> this
    }
    this < from -> from
    this > to -> to
    else -> this
}
