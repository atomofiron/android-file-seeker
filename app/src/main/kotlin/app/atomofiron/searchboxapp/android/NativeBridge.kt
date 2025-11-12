package app.atomofiron.searchboxapp.android

import android.content.Context
import android.os.Build
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.QueryParams
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.writeTo
import uniffi.native_lib.CancellationState
import uniffi.native_lib.Check
import uniffi.native_lib.CommonProgress
import uniffi.native_lib.CommonProgressCollector
import uniffi.native_lib.ComplexResult
import uniffi.native_lib.Meta
import uniffi.native_lib.MetaResult
import uniffi.native_lib.MetasResult
import uniffi.native_lib.NameSearchCollector
import uniffi.native_lib.NameSearchProgress
import uniffi.native_lib.SearchQuery
import uniffi.native_lib.SimpleResult
import uniffi.native_lib.SuCmd
import uniffi.native_lib.TextSearchCollector
import uniffi.native_lib.TextSearchProgress
import uniffi.native_lib.TypedMeta
import uniffi.native_lib.TypedMetaResult
import uniffi.native_lib.TypedMetasResult
import uniffi.native_lib.UsageResult
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

private const val NATIVE_BIN = "native-bin"
private const val NATIVE_LIB = "native_lib"
private const val NATIVE_LIB_SO = "lib$NATIVE_LIB.so"

private lateinit var suCmd: SuCmd

object NativeBridge {

    init {
        System.loadLibrary(NATIVE_LIB)
    }

    fun setSuCmd(cmd: String, binDir: String) {
        suCmd = SuCmd(cmd = cmd, binPath = "$binDir/$NATIVE_BIN")
    }

    fun trySu(): Rslt<Unit> {
        val response = uniffi.native_lib.tryAsSu(suCmd)
        return when (response) {
            is SimpleResult.Ok -> Rslt.Ok
            is SimpleResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun createFile(ref: NodeRef, asSu: Boolean): Rslt<Meta> {
        val response = uniffi.native_lib.createFile(ref.bytes, suCmd = suCmd.takeIf { asSu })
        return when (response) {
            is MetaResult.Ok -> Rslt.Ok(response.v1)
            is MetaResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun createDir(ref: NodeRef, asSu: Boolean): Rslt<Meta> {
        val response = uniffi.native_lib.createDir(ref.bytes, suCmd = suCmd.takeIf { asSu })
        return when (response) {
            is MetaResult.Ok -> Rslt.Ok(response.v1)
            is MetaResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun type(ref: NodeRef, asSu: Boolean): Rslt<TypedMeta> {
        val response = uniffi.native_lib.getFileType(ref.bytes, suCmd = suCmd.takeIf { asSu })
        return when (response) {
            is TypedMetaResult.Ok -> Rslt.Ok(response.v1)
            is TypedMetaResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun types(ref: NodeRef, asSu: Boolean): Rslt<List<TypedMeta>> {
        val response = uniffi.native_lib.getFileTypes(ref.bytes, suCmd = suCmd.takeIf { asSu })
        return when (response) {
            is TypedMetasResult.Ok -> Rslt.Ok(response.v1)
            is TypedMetasResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun meta(ref: NodeRef, asSu: Boolean): Rslt<Meta> {
        val response = uniffi.native_lib.getMeta(ref.bytes, suCmd = suCmd.takeIf { asSu })
        return when (response) {
            is MetaResult.Ok -> Rslt.Ok(response.v1)
            is MetaResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun metas(ref: NodeRef, asSu: Boolean): Rslt<List<Meta>> {
        val response = uniffi.native_lib.getMetas(ref.bytes, suCmd = suCmd.takeIf { asSu })
        return when (response) {
            is MetasResult.Ok -> Rslt.Ok(response.v1)
            is MetasResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun usage(ref: NodeRef, asSu: Boolean): Rslt<String> {
        val response = uniffi.native_lib.getUsage(ref.bytes, suCmd = suCmd.takeIf { asSu })
        return when (response) {
            is UsageResult.Ok -> Rslt.Ok(response.v1)
            is UsageResult.Err -> Rslt.Err(response.v1)
        }
    }

    fun delete(ref: NodeRef, asSu: Boolean): ComplexResult {
        val collector = object : CommonProgressCollector {
            override fun emit(progress: CommonProgress) = Unit
        }
        return uniffi.native_lib.deleteBy(ref.bytes, suCmd = suCmd.takeIf { asSu }, collector)
    }

    fun copy(
        from: NodeRef,
        to: NodeRef,
        move: Boolean = false,
        asSu: Boolean,
        collector: (CommonProgress) -> Unit,
    ): ComplexResult {
        val collector = object : CommonProgressCollector {
            override fun emit(progress: CommonProgress) = collector(progress)
        }
        return uniffi.native_lib.copy(from.bytes, to.bytes, move, suCmd = suCmd.takeIf { asSu }, collector)
    }

    fun findNames(
        params: QueryParams,
        targets: List<NodeRef>,
        maxDepth: Int,
        excludeDirs: Boolean,
        asSu: Boolean,
        cancellation: CancellationState,
        collector: (NameSearchProgress) -> Unit,
    ): SimpleResult {
        val collector = object : NameSearchCollector {
            override fun emit(progress: NameSearchProgress) = collector(progress)
        }
        val query = SearchQuery(params.query, params.regex, params.ignoreCase)
        return uniffi.native_lib.findNames(query, targets.map { it.bytes }, maxDepth.toUInt(), excludeDirs, suCmd = suCmd.takeIf { asSu }, cancellation, collector)
    }

    fun findLocalText(
        params: QueryParams,
        target: NodeRef,
        asSu: Boolean,
        cancellation: CancellationState,
    ): TextSearchProgress {
        var matches: TextSearchProgress = TextSearchProgress.Skip
        val result = findText(params, listOf(target), maxDepth = 1, Check.No, asSu, cancellation) {
            matches = it
        }
        return when (result) {
            is SimpleResult.Ok -> matches
            is SimpleResult.Err -> TextSearchProgress.Err(Meta(target.bytes, result.v1))
        }
    }

    fun findText(
        params: QueryParams,
        targets: List<NodeRef>,
        maxDepth: Int,
        maxSize: Long,
        asSu: Boolean,
        cancellation: CancellationState,
        collector: (TextSearchProgress) -> Unit,
    ): SimpleResult = findText(params, targets, maxDepth, Check.Yes(maxSize.toULong()), asSu, cancellation, collector)

    private fun findText(
        params: QueryParams,
        targets: List<NodeRef>,
        maxDepth: Int,
        check: Check,
        asSu: Boolean,
        cancellation: CancellationState,
        collector: (TextSearchProgress) -> Unit,
    ): SimpleResult {
        val collector = object : TextSearchCollector {
            override fun emit(progress: TextSearchProgress) = collector(progress)
        }
        val query = SearchQuery(params.query, params.regex, params.ignoreCase)
        return uniffi.native_lib.findText(query, targets.map { it.bytes }, maxDepth.toUInt(), check, suCmd = suCmd.takeIf { asSu }, cancellation, collector)
    }
}

fun Context.verifyNativeBin(): Rslt<Unit> {
    when (val lib = verifyNativeLib()) {
        is Rslt.Ok -> Unit
        is Rslt.Err -> return lib
    }
    val file = File(suCmd.binPath)
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

fun Context.verifyNativeLib(): Rslt<Unit> {
    val abi = Build.SUPPORTED_ABIS.firstOrNull()
    buildList {
        add(applicationInfo.sourceDir)
        applicationInfo.splitSourceDirs?.let { addAll(it) }
    }.forEach { path ->
        ZipFile(path).use { zip ->
            abi?.let { zip.getEntry("lib/$it/$NATIVE_LIB_SO") }
                ?.let { zip.getInputStream(it) }
                ?.use { input ->
                    val dest = File(filesDir, NATIVE_LIB_SO)
                    FileOutputStream(dest).use { input.copyTo(it) }
                    dest.setExecutable(true, true)
                    return Rslt.Ok
                }
        }
    }
    return Rslt.Err("no $NATIVE_LIB_SO found for abi=$abi")
}

private operator fun Meta.Companion.invoke(path: ByteArray, error: String): Meta {
    return Meta(path = path, access = "", owner = "", group = "", length = 0u, size = "", date = "", time = "", error = error)
}
