package app.atomofiron.common.util.property

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

open class WeakProperty<T : Any>(value: T? = null) : RoProperty<T?> {
    protected var reference = WeakReference<T>(value)

    override val value: T? get() = reference.get()

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = value

    open fun observe(observer: (T?) -> Unit) = observer(value)
}