package app.atomofiron.common.util.property

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

class LazyWeakProperty<T : Any> constructor(private val provider: () -> T) : Lazy<T> {
    private var reference = WeakReference<T>(null)

    override val value: T get() {
        var value = reference.get()
        if (value == null) {
            value = provider()
            reference = WeakReference(value)
        }
        return value
    }

    operator fun getValue(any: Any, property: KProperty<*>): T = value

    override fun isInitialized(): Boolean = reference.get() != null
}