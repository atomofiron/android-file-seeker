package app.atomofiron.searchboxapp.android

import android.content.Context
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.writeTo
import uniffi.native_lib.ComplexResult
import uniffi.native_lib.Meta
import uniffi.native_lib.MetaResult
import uniffi.native_lib.MetasResult
import uniffi.native_lib.Progress
import uniffi.native_lib.ProgressCollector
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

    fun createFile(ref: NodeRef, asSu: Boolean): Rslt<Meta> {
        val response = uniffi.native_lib.createFile(ref.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is MetaResult.Ok -> Rslt.Ok(response.v1)
            is MetaResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun createDir(ref: NodeRef, asSu: Boolean): Rslt<Meta> {
        val response = uniffi.native_lib.createDir(ref.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is MetaResult.Ok -> Rslt.Ok(response.v1)
            is MetaResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun type(ref: NodeRef, asSu: Boolean): Rslt<TypedMeta> {
        val response = uniffi.native_lib.getFileType(ref.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is TypedMetaResult.Ok -> Rslt.Ok(response.v1)
            is TypedMetaResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun types(ref: NodeRef, asSu: Boolean): Rslt<List<TypedMeta>> {
        val response = uniffi.native_lib.getFileTypes(ref.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is TypedMetasResult.Ok -> Rslt.Ok(response.v1)
            is TypedMetasResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun meta(ref: NodeRef, asSu: Boolean): Rslt<Meta> {
        val response = uniffi.native_lib.getMeta(ref.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is MetaResult.Ok -> Rslt.Ok(response.v1)
            is MetaResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun metas(ref: NodeRef, asSu: Boolean): Rslt<List<Meta>> {
        val response = uniffi.native_lib.getMetas(ref.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is MetasResult.Ok -> Rslt.Ok(response.v1)
            is MetasResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun usage(ref: NodeRef, asSu: Boolean): Rslt<String> {
        val response = uniffi.native_lib.getUsage(ref.bytes, runAsSu = binPath.takeIf { asSu })
        return when (response) {
            is UsageResult.Ok -> Rslt.Ok(response.v1)
            is UsageResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun delete(ref: NodeRef, asSu: Boolean): ComplexResult {
        val collector = object : ProgressCollector {
            override fun emit(progress: Progress) = Unit
        }
        return uniffi.native_lib.deleteBy(ref.bytes, runAsSu = binPath.takeIf { asSu }, collector)
    }

    fun copy(
        from: NodeRef,
        to: NodeRef,
        move: Boolean = false,
        asSu: Boolean,
        collector: (Progress) -> Unit,
    ): ComplexResult {
        val collector = object : ProgressCollector {
            override fun emit(progress: Progress) = collector(progress)
        }
        return uniffi.native_lib.copy(from.bytes, to.bytes, move, runAsSu = binPath.takeIf { asSu }, collector)
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
