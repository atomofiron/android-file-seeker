package app.atomofiron.common.util.property

import kotlin.reflect.KProperty

class MutableStrongProperty<T : Any?> : StrongProperty<T> {
    override var value: T
        get() = super.value
        public set(value) {
            super.value = value
        }

    constructor() : super()

    constructor(value: T) : super(value)

    operator fun setValue(any: Any, property: KProperty<*>, value: T) {
        this.value = value
    }
}