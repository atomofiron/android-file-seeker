package app.atomofiron.common.util

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CoroutineSafeList<E> private constructor(
    private val mutable: MutableList<E>,
) : List<E> by mutable {

    private val mutex = Mutex()

    constructor() : this(mutableListOf())

    suspend fun add(element: E): Boolean = locked {
        mutable.add(element)
    }

    suspend fun remove(element: E): Boolean = locked {
        mutable.remove(element)
    }

    suspend fun add(index: Int, element: E) = locked {
        mutable.add(index, element)
    }

    suspend fun removeAt(index: Int): E = locked {
        mutable.removeAt(index)
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    suspend fun addLast(element: E) = locked {
        mutable.addLast(element)
    }

    suspend fun addAll(elements: Collection<E>): Boolean = locked {
        mutable.addAll(elements)
    }

    suspend fun addAll(index: Int, elements: Collection<E>): Boolean = locked {
        mutable.addAll(index, elements)
    }

    suspend fun removeAll(elements: Collection<E>): Boolean = locked {
        mutable.removeAll(elements)
    }

    suspend fun retainAll(elements: Collection<E>): Boolean = locked {
        mutable.retainAll(elements)
    }

    suspend fun clear() = locked {
        mutable.clear()
    }

    suspend fun set(index: Int, element: E): E = locked {
        mutable.set(index, element)
    }

    private suspend inline fun <T> locked(action: () -> T): T = mutex.withLock(action = action)
}
