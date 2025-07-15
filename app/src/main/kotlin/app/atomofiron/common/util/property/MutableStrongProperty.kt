package app.atomofiron.common.util.property

import kotlin.reflect.KProperty

open class MutableStrongProperty<T : Any?>(value: T) : StrongProperty<T>(value) {

    override var value: T
        get() = super.value
        public set(value) {
            super.value = value
        }

    operator fun setValue(any: Any, property: KProperty<*>, value: T) {
        this.value = value
    }
}