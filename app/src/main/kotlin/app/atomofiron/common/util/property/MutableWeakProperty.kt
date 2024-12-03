package app.atomofiron.common.util.property

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

class MutableWeakProperty<T : Any> constructor(value: T? = null) : WeakProperty<T>(value) {
    override var value: T?
        get() = reference.get()
        set(value) {
            reference = WeakReference<T>(value)
            setAttached(value)
        }

    private val observers = mutableListOf<(T?) -> Unit>()

    operator fun setValue(any: Any, property: KProperty<*>, value: T?) {
        this.value = value
    }

    inline operator fun <R> invoke(crossinline action: (value: T) -> R): R? = value?.run(action)

    override fun observe(observer: (T?) -> Unit) {
        super.observe(observer)
        observers.add(observer)
    }

    private fun setAttached(value: T?) {
        observers.forEach { observer ->
            observer(value)
        }
    }
}