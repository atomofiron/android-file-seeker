package app.atomofiron.common.util.extension

import android.content.Context
import android.widget.Toast
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.simpleName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
