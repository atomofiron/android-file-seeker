package app.atomofiron.searchboxapp.android

import android.content.Context
import app.atomofiron.searchboxapp.android.NativeBridge.binPath
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.Shell
import app.atomofiron.searchboxapp.utils.writeTo
import uniffi.native_lib.DeleteResult
import uniffi.native_lib.Meta
import uniffi.native_lib.MetaResult
import uniffi.native_lib.MetasResult
import uniffi.native_lib.TypedMeta
import uniffi.native_lib.TypedMetaResult
import uniffi.native_lib.TypedMetasResult
import uniffi.native_lib.UsageResult
import java.io.File
import java.io.FileOutputStream

private const val NATIVE_BIN = "native-bin"

// todo asSu
object NativeBridge {

    var binPath = ""
        private set

    init {
        System.loadLibrary("native_lib")
    }

    fun setBinDir(path: String) {
        binPath = "$path/$NATIVE_BIN"
    }

    fun createFile(path: String, asSu: Boolean): Rslt<Meta> {
        val response = uniffi.native_lib.createFile(path)
        return when (response) {
            is MetaResult.Ok -> Rslt.Ok(response.v1)
            is MetaResult.Error -> Rslt.Err(response.v1)
        }
    }

    fun createDir(path: String, asSu: Boolean): Rslt<Meta> {
        val response = uniffi.native_lib.createDir(path)
        return when (response) {
            is MetaResult.Ok -> Rslt.Ok(response.v1)
            is MetaResult.Error -> Rslt.Err(response.v1)
        }
    }

    fun type(path: String, asSu: Boolean): Rslt<TypedMeta> {
        val response = uniffi.native_lib.getFileType(path)
        return when (response) {
            is TypedMetaResult.Ok -> Rslt.Ok(response.v1)
            is TypedMetaResult.Error -> Rslt.Err(response.v1)
        }
    }

    fun types(path: String, asSu: Boolean): Rslt<List<TypedMeta>> {
        val response = uniffi.native_lib.getFileTypes(path)
        return when (response) {
            is TypedMetasResult.Ok -> Rslt.Ok(response.v1)
            is TypedMetasResult.Error -> Rslt.Err(response.v1)
        }
    }

    fun meta(path: String, asSu: Boolean): Rslt<Meta> {
        val response = uniffi.native_lib.getMeta(path)
        return when (response) {
            is MetaResult.Ok -> Rslt.Ok(response.v1)
            is MetaResult.Error -> Rslt.Err(response.v1)
        }
    }

    fun metas(path: String, asSu: Boolean): Rslt<List<Meta>> {
        val response = uniffi.native_lib.getMetas(path)
        return when (response) {
            is MetasResult.Ok -> Rslt.Ok(response.v1)
            is MetasResult.Error -> Rslt.Err(response.v1)
        }
    }

    fun usage(path: String, asSu: Boolean): Rslt<String> {
        val response = uniffi.native_lib.getUsage(path)
        return when (response) {
            is UsageResult.Ok -> Rslt.Ok(response.v1)
            is UsageResult.Error -> Rslt.Err(response.v1)
        }
    }

    fun delete(path: String, asSu: Boolean): Rslt<Unit> {
        val response = uniffi.native_lib.deleteBy(path)
        return when (response) {
            is DeleteResult.Ok -> Rslt.Ok
            is DeleteResult.Error -> Rslt.Err(response.v1)
        }
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
        if (output.success) {
            errorMessageBuilder.clear()
            break
        } else {
            errorMessageBuilder.append(output.error)
            errorMessageBuilder.append("\n")
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
