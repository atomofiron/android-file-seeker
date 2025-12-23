package app.atomofiron.common.util

import android.os.Looper
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.common.util.extension.hash

private var nextId = 0

class Unique<T>(val value: T) : Equality {

    private val uniqueId = nextId++

    init {
        debugRequire(Looper.getMainLooper().isCurrentThread)
    }

    override fun hashCode(): Int = hash(this::class, uniqueId)

    override fun equals(other: Any?): Boolean = (other as? Unique<*>)?.uniqueId == uniqueId
}

interface Equality {
    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
}