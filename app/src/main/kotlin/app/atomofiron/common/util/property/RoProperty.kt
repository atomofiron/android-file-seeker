package app.atomofiron.common.util.property

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface RoProperty<T> : ReadOnlyProperty<Any?, T> {
    companion object {
        fun <T : R, R> RoProperty<T>.map(): RoProperty<R> = object : RoProperty<R> {
            override val value: R get() = this@map.value
        }
    }

    val value: T

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    fun <R> map(map: (T) -> R): RoProperty<R> = object : RoProperty<R> {
        override val value: R get() = this@RoProperty.value.let(map)
    }
}