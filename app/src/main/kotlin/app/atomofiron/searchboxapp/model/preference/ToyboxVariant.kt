package app.atomofiron.searchboxapp.model.preference

import android.content.Context
import app.atomofiron.searchboxapp.utils.Const

class ToyboxVariant(
    context: Context,
    val variant: String,
    val customPath: String,
) {
    companion object {
        fun fromSet(context: Context, set: Set<String>): ToyboxVariant {
            require(set.size == 2) { IllegalArgumentException() }

            val variants = arrayOf(
                    Const.VALUE_TOYBOX_CUSTOM,
                    Const.VALUE_TOYBOX_ARM_64,
                    Const.VALUE_TOYBOX_ARM_32,
                    Const.VALUE_TOYBOX_X86_64
            )
            val first = set.first()
            val last = set.last()
            return when {
                variants.contains(first) -> ToyboxVariant(context, first, last)
                variants.contains(last) -> ToyboxVariant(context, last, first)
                else -> throw IllegalArgumentException()
            }
        }

        fun getToyboxPath(context: Context, variant: String): String = when (variant) {
            Const.VALUE_TOYBOX_ARM_32 -> context.filesDir.absolutePath + Const.TOYBOX_32
            Const.VALUE_TOYBOX_ARM_64 -> context.filesDir.absolutePath + Const.TOYBOX_64
            Const.VALUE_TOYBOX_X86_64 -> context.filesDir.absolutePath + Const.TOYBOX_86_64
            Const.VALUE_TOYBOX_IMPORTED -> context.filesDir.absolutePath + Const.TOYBOX_IMPORTED
            else -> throw Exception("Unknown variant $variant")
        }
    }

    val toyboxPath = when (variant) {
        Const.VALUE_TOYBOX_CUSTOM -> customPath
        else -> getToyboxPath(context, variant)
    }
}