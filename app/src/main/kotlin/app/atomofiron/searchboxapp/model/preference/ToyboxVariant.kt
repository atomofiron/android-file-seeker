package app.atomofiron.searchboxapp.model.preference

import app.atomofiron.searchboxapp.utils.Const

sealed class ToyboxVariant(open val path: String) {
    data object Undefined : ToyboxVariant(Const.DEFAULT_TOYBOX_PATH) {
        const val EMPTY = ""
    }
    data class Embedded(override val path: String) : ToyboxVariant(path) {
        companion object {
            val Stub = Embedded("placeholder")
        }
    }
    data object System : ToyboxVariant(Const.DEFAULT_TOYBOX_PATH)

    companion object {
        operator fun invoke(path: String) = when (path) {
            Undefined.EMPTY -> Undefined
            System.path -> System
            else -> Embedded(path)
        }
        operator fun invoke(embedded: Boolean) = if (embedded) Embedded.Stub else System
    }
}