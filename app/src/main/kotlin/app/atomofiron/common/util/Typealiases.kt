package app.atomofiron.common.util

import android.app.Activity
import android.os.Build.VERSION_CODES
import app.atomofiron.common.util.property.RoProperty
import kotlin.collections.removeLast as dropLast

typealias AndroidSdk = VERSION_CODES

fun <T> MutableList<T>.dropLast() = dropLast()

typealias MaterialId = com.google.android.material.R.id
typealias MaterialAttr = com.google.android.material.R.attr
typealias MaterialColor = com.google.android.material.R.color
typealias MaterialDimen = com.google.android.material.R.dimen

typealias Unreachable = Unit
typealias UnreachableException = Exception
typealias ActivityProperty = RoProperty<out Activity?>

fun <T> unsafeLazy(initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)

fun <T> MutableList(size: Int): MutableList<T> = ArrayList(size)
