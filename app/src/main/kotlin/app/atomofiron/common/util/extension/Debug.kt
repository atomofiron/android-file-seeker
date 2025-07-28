package app.atomofiron.common.util.extension

import app.atomofiron.fileseeker.BuildConfig

fun debugRequire(value: Boolean, lazyMessage: (() -> Any)? = null) = when {
    !BuildConfig.DEBUG_BUILD -> Unit
    lazyMessage == null -> require(value)
    else -> require(value, lazyMessage)
}

fun Any?.debugRequireNotNull(lazyMessage: (() -> Any)? = null) = debugRequire(this != null, lazyMessage)
