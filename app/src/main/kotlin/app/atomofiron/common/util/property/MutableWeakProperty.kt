package app.atomofiron.common.util.property

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

class MutableWeakProperty<T : Any>(value: T? = null) : WeakProperty<T>(value) {

    override var value: T?
        get() = reference.get()
        set(value) { reference = WeakReference<T>(value) }

    operator fun setValue(any: Any, property: KProperty<*>, value: T) {
        this.value = value
    }
}