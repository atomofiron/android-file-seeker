package app.atomofiron.searchboxapp.utils

import android.content.Context
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import java.util.regex.Pattern

private const val TOYBOX = "toybox"
private val MinVersion = listOf("0", "7", "4") // may be lower

private fun Context.embedded() = assets.list(TOYBOX) ?: arrayOf()

fun Context.verify(embedded: ToyboxVariant): ToyboxVariant = when (embedded) {
    is ToyboxVariant.Undefined -> getCorrectVariant()
    is ToyboxVariant.Embedded -> selectEmbedded() ?: ToyboxVariant.System
    is ToyboxVariant.System -> ToyboxVariant.System
}

private fun Context.getCorrectVariant(): ToyboxVariant {
    val version = getToyboxVersion(ToyboxVariant.System.path)
    return takeIf { version.lowerThan(MinVersion) }
        ?.selectEmbedded()
        ?: ToyboxVariant.System
}

private fun List<String>?.lowerThan(other: List<String>): Boolean {
    this ?: return true
    for (i in 0..max(size, other.size)) {
        val first = getOrNull(i) ?: return true
        val second = other.getOrNull(i) ?: return false
        when {
            first == second -> continue
            else -> return first < second
        }
    }
    return false
}

private fun getToyboxVersion(path: String): List<String>? {
    val cmd = Shell[Shell.VERSION, path]
    val out = Shell.exec(cmd, false)
    val pattern = Pattern.compile("[0-9.]+")
    val matcher = pattern.matcher(out.output)
    return matcher
        .takeIf { it.find() }
        ?.group()
        ?.split(Const.DOT)
}

private fun Context.selectEmbedded(): ToyboxVariant.Embedded? {
    filesDir.mkdirs()
    val path = "${filesDir.absolutePath}/$TOYBOX"
    if (getToyboxVersion(path) != null) {
        return ToyboxVariant.Embedded(path)
    }
    val file = File(path)
    for (name in embedded()) {
        val input = assets.open("$TOYBOX/$name")
        if (file.length().toInt() != input.available()) {
            val output = FileOutputStream(file)
            input.writeTo(output)
            output.close()
        }
        input.close()
        if (!file.canExecute()) {
            file.setExecutable(true, true)
        }
        if (getToyboxVersion(path) != null) {
            return ToyboxVariant.Embedded(path)
        }
    }
    file.delete()
    return null
}
