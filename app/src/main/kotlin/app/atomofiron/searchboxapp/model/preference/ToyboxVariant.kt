package app.atomofiron.searchboxapp.model.preference

private const val SYSTEM_TOYBOX_PATH = "/system/bin/toybox"

sealed class ToyboxVariant(open val path: String) {
    data object Undefined : ToyboxVariant(SYSTEM_TOYBOX_PATH) {
        const val EMPTY = ""
    }
    data class Embedded(override val path: String) : ToyboxVariant(path) {
        companion object {
            val Stub = Embedded("placeholder")
        }
    }
    data object System : ToyboxVariant(SYSTEM_TOYBOX_PATH)

    companion object {
        operator fun invoke(path: String) = when (path) {
            Undefined.EMPTY -> Undefined
            System.path -> System
            else -> Embedded(path)
        }
        operator fun invoke(embedded: Boolean) = if (embedded) Embedded.Stub else System
    }
}