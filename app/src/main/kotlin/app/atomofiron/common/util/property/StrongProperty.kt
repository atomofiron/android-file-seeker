package app.atomofiron.common.util.property

import kotlin.reflect.KProperty

open class StrongProperty<T : Any?>() {
    private var nullable: T? = null

    open var value: T
        get() = nullable as T
        protected set(value) {
            nullable = value
        }

    constructor(value: T) : this() {
        nullable = value
    }

    operator fun getValue(any: Any, property: KProperty<*>): T = value
}