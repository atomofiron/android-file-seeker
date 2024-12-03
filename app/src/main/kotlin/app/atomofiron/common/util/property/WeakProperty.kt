package app.atomofiron.common.util.property

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

open class WeakProperty<T : Any> constructor(value: T? = null) {
    protected var reference = WeakReference<T>(value)

    open val value: T? get() = reference.get()

    operator fun getValue(any: Any, property: KProperty<*>): T? = value

    open fun observe(observer: (T?) -> Unit) = observer(value)
}