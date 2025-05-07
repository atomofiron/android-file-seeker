package app.atomofiron.common.util

import android.graphics.drawable.Drawable
import android.view.View
import java.lang.ref.WeakReference

class WeakDrawableCallback(view: View) : Drawable.Callback {

    private val reference = WeakReference(view)
    val view: View? get() = reference.get()
    val isEmpty get() = view == null

    override fun invalidateDrawable(who: Drawable) {
        reference.get()?.invalidate()
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) = Unit

    override fun unscheduleDrawable(who: Drawable, what: Runnable) = Unit
}