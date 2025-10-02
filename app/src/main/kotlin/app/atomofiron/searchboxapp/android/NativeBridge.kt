package app.atomofiron.searchboxapp.android

import android.content.Context
import app.atomofiron.searchboxapp.model.explorer.NodePath
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.writeTo
import uniffi.native_lib.DeleteResult
import uniffi.native_lib.Meta
import uniffi.native_lib.MetaResult
import uniffi.native_lib.MetasResult
import uniffi.native_lib.SimpleResult
import uniffi.native_lib.TypedMeta
import uniffi.native_lib.TypedMetaResult
import uniffi.native_lib.TypedMetasResult
import uniffi.native_lib.UsageResult
import java.io.File
import java.io.FileOutputStream

private const val NATIVE_BIN = "native-bin"

private var binPath = ""

object NativeBridge {

    init {
        System.loadLibrary("native_lib")
    }

    fun setBinDir(path: String) {
        binPath = "$path/$NATIVE_BIN"
    }

    fun trySu(): Rslt<Unit> {
        val response = uniffi.native_lib.tryAsSu(binPath)
        return when (response) {
            is SimpleResult.Ok -> Rslt.Ok
            is SimpleResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun createFile(path: NodePath, asSu: Boolean): Rslt<Meta> {
        val response = uniffi.native_lib.createFile(path.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is MetaResult.Ok -> Rslt.Ok(response.v1)
            is MetaResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun createDir(path: NodePath, asSu: Boolean): Rslt<Meta> {
        val response = uniffi.native_lib.createDir(path.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is MetaResult.Ok -> Rslt.Ok(response.v1)
            is MetaResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun type(path: NodePath, asSu: Boolean): Rslt<TypedMeta> {
        val response = uniffi.native_lib.getFileType(path.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is TypedMetaResult.Ok -> Rslt.Ok(response.v1)
            is TypedMetaResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun types(path: NodePath, asSu: Boolean): Rslt<List<TypedMeta>> {
        val response = uniffi.native_lib.getFileTypes(path.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is TypedMetasResult.Ok -> Rslt.Ok(response.v1)
            is TypedMetasResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun meta(path: NodePath, asSu: Boolean): Rslt<Meta> {
        val response = uniffi.native_lib.getMeta(path.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is MetaResult.Ok -> Rslt.Ok(response.v1)
            is MetaResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun metas(path: NodePath, asSu: Boolean): Rslt<List<Meta>> {
        val response = uniffi.native_lib.getMetas(path.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is MetasResult.Ok -> Rslt.Ok(response.v1)
            is MetasResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun usage(path: NodePath, asSu: Boolean): Rslt<String> {
        val response = uniffi.native_lib.getUsage(path.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is UsageResult.Ok -> Rslt.Ok(response.v1)
            is UsageResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun delete(path: NodePath, asSu: Boolean): Rslt<UInt> {
        val response = uniffi.native_lib.deleteBy(path.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is DeleteResult.Ok -> Rslt.Ok(0u)
            is DeleteResult.Err -> Rslt.Err(response.v1)
            is DeleteResult.ErrCount -> Rslt.Ok(response.v1)
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
        val result = NativeBridge.trySu()
        when (result) {
            is Rslt.Ok -> {
                errorMessageBuilder.clear()
                break
            }
            is Rslt.Err -> {
                errorMessageBuilder.append(result.message)
                errorMessageBuilder.append("\n")
                file.delete()
            }
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
