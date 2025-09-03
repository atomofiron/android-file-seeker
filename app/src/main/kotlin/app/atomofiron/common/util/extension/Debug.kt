package app.atomofiron.common.util.extension

import app.atomofiron.fileseeker.BuildConfig

inline fun debugRequire(predicate: () -> Boolean) {
    if (BuildConfig.DEBUG_BUILD) debugRequire(predicate())
}

fun debugRequire(value: Boolean)  {
    if (BuildConfig.DEBUG_BUILD) require(value)
}

inline fun debugRequire(value: Boolean, lazyMessage: () -> Any)  {
    if (BuildConfig.DEBUG_BUILD) require(value, lazyMessage)
}

fun Any?.debugRequireNotNull(lazyMessage: (() -> Any)? = null) = when (lazyMessage) {
    null -> debugRequire(this != null)
    else -> debugRequire(this != null, lazyMessage)
}
