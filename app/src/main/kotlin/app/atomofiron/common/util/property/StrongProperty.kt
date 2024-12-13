package app.atomofiron.common.util.property

import kotlin.reflect.KProperty

open class StrongProperty<T : Any?>() : RoProperty<T> {
    private var nullable: T? = null

    override var value: T
        get() = nullable as T
        protected set(value) {
            nullable = value
        }

    constructor(value: T) : this() {
        nullable = value
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}