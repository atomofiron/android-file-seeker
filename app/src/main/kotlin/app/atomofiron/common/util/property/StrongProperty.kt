package app.atomofiron.common.util.property

import kotlin.reflect.KProperty

open class StrongProperty<T : Any?>(value: T) : RoProperty<T> {

    private var nullable: T = value

    override var value: T
        get() = nullable as T
        protected set(value) {
            nullable = value
        }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}