package app.atomofiron.searchboxapp.utils

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import app.atomofiron.common.util.property.MutableStrongProperty
import app.atomofiron.common.util.property.StrongProperty
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

fun MaterialButtonToggleGroup.check(@IdRes id: Int, checked: Boolean) {
    if (checked) check(id) else uncheck(id)
}

inline fun View.updateMarginLayoutParams(block: ViewGroup.MarginLayoutParams.(Resources) -> Unit) {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.block(resources)
    layoutParams = params
}

fun delayed(delay: Long = 0, action: suspend () -> Unit): (CoroutineScope) -> Unit {
    var job: Job? = null
    return { scope ->
        job?.cancel()
        job = scope.launch {
            delay(delay.milliseconds)
            action()
            job = null
        }
    }
}

fun View.scope(): StrongProperty<CoroutineScope> {
    val scope = CoroutineScope(Dispatchers.Main)
    scope.cancel()
    val property = MutableStrongProperty(scope)
    addOnAttachListener(
        onAttach = { property.value = CoroutineScope(Dispatchers.Main) },
        onDetach = { property.value.cancel() }
    )
    return property
}

fun View.onResize(action: (View) -> Unit) {
    addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
        if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) {
            action(view)
        }
    }
}
