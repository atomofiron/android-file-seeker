package app.atomofiron.searchboxapp.android

import android.content.Context
import app.atomofiron.searchboxapp.android.NativeBridge.binPath
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.Shell
import app.atomofiron.searchboxapp.utils.writeTo
import java.io.File
import java.io.FileOutputStream

private const val NATIVE_BIN = "native-bin"

object NativeBridge {

    var binPath = ""
        private set

    init {
        System.loadLibrary("native_lib")
    }

    external fun run(command: String, args: Array<String>): ByteArray
    external fun runAsync(command: String, args: Array<String>, callback: (ByteArray) -> Unit)

    fun setBinDir(path: String) {
        binPath = "$path/$NATIVE_BIN"
    }
}

fun Context.verifyNativeBin(): Rslt<Unit> {
    val file = File(binPath)
    file.parentFile?.mkdirs()
    val embedded = assets.list(NATIVE_BIN)
        ?.sortedBy { !it.endsWith("64") }
        ?.map { assets.open("$NATIVE_BIN/$it") }
    when {
        embedded == null -> return Rslt.Err("Asset list is null")
        embedded.isEmpty() -> return Rslt.Err("Binaries not found")
    }
    val isOutOfDate = embedded.none { stream ->
        stream.available().toLong() == file.length()
    }
    if (isOutOfDate) {
        file.delete()
    }
    val errorMessageBuilder = StringBuilder()
    if (!file.exists()) for (stream in embedded) {
        FileOutputStream(file).use {
            stream.writeTo(it)
        }
        file.setExecutable(true, true)
        val output = Shell.checkSu(binPath)
        errorMessageBuilder.append(output.error)
        errorMessageBuilder.append("\n")
        if (output.success) {
            errorMessageBuilder.clear()
            break
        } else {
            file.delete()
        }
    }
    for (stream in embedded) {
        stream.close()
    }
    val message = errorMessageBuilder.trim().toString()
    return when {
        message.isEmpty() -> Rslt.Ok
        else -> Rslt.Err(message)
    }
}
