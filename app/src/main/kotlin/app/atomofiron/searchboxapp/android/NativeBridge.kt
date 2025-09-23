package app.atomofiron.searchboxapp.android

import android.content.Context
import app.atomofiron.searchboxapp.android.NativeBridge.binPath
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.Shell
import app.atomofiron.searchboxapp.utils.writeTo
import bridge.Bridge
import bridge.commandMsg
import java.io.File
import java.io.FileOutputStream

private const val NATIVE_BIN = "native-bin"

object NativeBridge {

    var binPath = ""
        private set

    init {
        System.loadLibrary("native_lib")
    }

    external fun run(command: ByteArray, vararg args: String): ByteArray
    external fun runAsync(command: ByteArray, vararg args: String, callback: (ByteArray) -> Unit)

    fun setBinDir(path: String) {
        binPath = "$path/$NATIVE_BIN"
    }

    fun createFile(path: String, asSu: Boolean): Rslt<Bridge.Meta> = run(Bridge.Command.MKFILE, asSu, path)

    fun createDir(path: String, asSu: Boolean): Rslt<Bridge.Meta> = run(Bridge.Command.MKDIR, asSu, path)

    fun type(path: String, asSu: Boolean): Rslt<Bridge.TypeEntry> = run(Bridge.Command.TYPE, asSu, path)

    fun types(path: String, asSu: Boolean): Rslt<List<Bridge.TypeEntry>> = run(Bridge.Command.TYPES, asSu, path)

    fun meta(path: String, asSu: Boolean): Rslt<Bridge.Meta> = run(Bridge.Command.META, asSu, path)

    fun metas(path: String, asSu: Boolean): Rslt<List<Bridge.Meta>> = run(Bridge.Command.METAS, asSu, path)

    fun usage(path: String, asSu: Boolean): Rslt<String> = run(Bridge.Command.USAGE, asSu, path)

    fun delete(path: String, asSu: Boolean): Rslt<Unit> = run(Bridge.Command.DELETE, asSu, path)

    private inline fun <reified R> run(command: Bridge.Command, asSu: Boolean, vararg args: String): Rslt<R> {
        val msg = commandMsg { cmd = command }
        // todo asSu
        val bytes = run(msg.toByteArray(), *args)
        val result = Bridge.ResultMsg.parseFrom(bytes)
        val data = when (result.dataCase) {
            Bridge.ResultMsg.DataCase.META -> result.meta
            Bridge.ResultMsg.DataCase.METAS -> result.metas.entriesList
            Bridge.ResultMsg.DataCase.TYPE -> result.type
            Bridge.ResultMsg.DataCase.TYPES -> result.types.entriesList
            Bridge.ResultMsg.DataCase.USAGE -> result.usage
            Bridge.ResultMsg.DataCase.ERROR -> return Rslt.Err(result.error)
            Bridge.ResultMsg.DataCase.DATA_NOT_SET -> Unit
        }
        return Rslt.Ok(data as R)
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
