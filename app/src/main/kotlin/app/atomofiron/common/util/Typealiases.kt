package app.atomofiron.common.util

import android.os.Build.VERSION_CODES
import kotlin.collections.removeLast as dropLast
import kotlin.collections.removeLast as kotlinRemoveLast

typealias AndroidSdk = VERSION_CODES

fun <T> MutableList<T>.dropLast() = dropLast()
fun <T> MutableList<T>.removeLastOne() = kotlinRemoveLast()

typealias MaterialId = com.google.android.material.R.id
typealias MaterialAttr = com.google.android.material.R.attr
typealias MaterialDimen = com.google.android.material.R.dimen

typealias Unreachable = Unit
