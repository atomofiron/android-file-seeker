package app.atomofiron.common.util

import android.os.Build.VERSION_CODES
import kotlin.collections.removeLast as dropLast

typealias AndroidSdk = VERSION_CODES

fun <T> MutableList<T>.dropLast() = dropLast()

typealias MaterialId = com.google.android.material.R.id
typealias MaterialAttr = com.google.android.material.R.attr
typealias MaterialDimen = com.google.android.material.R.dimen

typealias Unreachable = Unit
typealias UnreachableException = Exception

fun <T> unsafeLazy(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)
