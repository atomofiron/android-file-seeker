package app.atomofiron.common.util

import android.graphics.drawable.Drawable
import android.view.View
import java.lang.ref.WeakReference

class WeakDrawableCallback(view: View) : Drawable.Callback {

    private val reference = WeakReference(view)

    override fun invalidateDrawable(who: Drawable) {
        reference.get()?.invalidate()
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) = Unit

    override fun unscheduleDrawable(who: Drawable, what: Runnable) = Unit
}