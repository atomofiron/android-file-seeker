package app.atomofiron.common.util.extension

import android.content.Context
import android.util.Log
import android.widget.Toast
import app.atomofiron.fileseeker.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

var debugContext = WeakReference<Context>(null)

inline fun Any.debugFail(lazyMessage: () -> Any) = debugRequire(false, lazyMessage)

inline fun Any.debugRequireNotNull(any: Any?, lazyMessage: () -> Any) = debugRequire(any != null, lazyMessage)

inline fun Any.debugRequire(value: Boolean, lazyMessage: () -> Any)  {
    if (BuildConfig.DEBUG_BUILD) require(value) {
        val message = "$simpleName: ${lazyMessage()}"
        debugContext.get()?.let {
            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(it, message, Toast.LENGTH_LONG).show()
            }
        }
        message
    }
}

suspend fun debugDelay(seconds: Int = 1) = if (BuildConfig.DEBUG) delay(seconds * 1000L) else Unit

val Any?.simpleName: String get() = when {
    this == null -> null
    else -> this::class.java.simpleName
}.toString()

val Any?.className: String get() = when {
    this == null -> null
    else -> this::class.java.name
}.toString()

fun Any.logE(s: String) {
    if (!BuildConfig.DEBUG) {
        // reportError(s, null)
    }
    Log.e("searchboxapp", "[ERROR in ${this.simpleName}] $s")
}
