package app.atomofiron.searchboxapp

import kotlin.collections.removeLast as dropLast
import kotlin.collections.removeLast as kotlinRemoveLast

fun <T> MutableList<T>.dropLast() = dropLast()
fun <T> MutableList<T>.removeLastOne() = kotlinRemoveLast()

typealias MaterialId = com.google.android.material.R.id
typealias MaterialAttr = com.google.android.material.R.attr
typealias MaterialDimen = com.google.android.material.R.dimen

typealias Unreachable = Unit
