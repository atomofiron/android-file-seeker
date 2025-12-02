package app.atomofiron.searchboxapp.utils

import android.view.View

open class BindingCache {

    private val cache = mutableMapOf<Int, Any?>()

    protected inline fun <V : View, T> V.bind(data: T, action: V.(T) -> Unit) {
        if (updateCache(id, data)) {
            action(data)
        }
    }

    /** @return true if cache was updated with a new one */
    protected fun <T> updateCache(id: Int, data: T): Boolean {
        val current = cache[id]
        cache[id] = data
        return data != current
    }
}
