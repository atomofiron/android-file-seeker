package app.atomofiron.searchboxapp.utils

import android.content.pm.PackageManager
import app.atomofiron.common.util.MutableList
import app.atomofiron.common.util.extension.debugFail
import app.atomofiron.common.util.extension.logE
import app.atomofiron.common.util.extension.takeIfDebug
import app.atomofiron.common.util.forHumans
import app.atomofiron.common.util.property.MutableWeakProperty
import app.atomofiron.searchboxapp.android.NativeBridge
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.DirectoryKind
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.Node.Companion.stateStub
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeContent.AndroidApp
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeOperation
import app.atomofiron.searchboxapp.model.explorer.NodeProperties
import app.atomofiron.searchboxapp.model.explorer.NodeRootType
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.explorer.NodeStateImpl
import app.atomofiron.searchboxapp.model.explorer.other.forNode
import app.atomofiron.searchboxapp.utils.Const.LF
import kotlinx.coroutines.Job
import uniffi.native_lib.ComplexResult
import uniffi.native_lib.Meta
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.math.roundToInt

object ExplorerUtils {
    // зато насколько всё становится проще
    val packageManager = MutableWeakProperty<PackageManager>()

    private const val DIR_CHAR = 'd'
    private const val LINK_CHAR = 'l'
    private const val FILE_CHAR = '-'
    private const val LS_NO_SUCH_FILE = "No such file or directory"
    private const val LS_PERMISSION_DENIED = "Permission denied"

    private const val DIRECTORY = NodeContent.Directory.MIME_TYPE
    private const val FILE_PICTURE = "image/"
    private const val FILE_AUDIO = "audio/"
    private const val FILE_VIDEO = "video/"
    private const val FILE_TEXT_SCRIPT = "text/x-"
    private const val FILE_TEXT = "text/"
    private const val FILE_MESSAGE = "message/rfc822"
    private const val FILE_UNKNOWN = "application/octet-stream"
    private const val FILE_XML = "application/xml"
    private const val FILE_RAR = "application/vnd.rar"
    const val FILE_ZIP = "application/zip"
    const val FILE_APK = "application/vnd.android.package-archive"
    private const val FILE_GZIP = "application/gzip"
    private const val FILE_JAVA = "application/java-vm"
    private const val FILE_XZ = "application/x-xz"
    private const val FILE_BZIP2 = "application/x-bzip2"
    private const val FILE_TAR = "application/x-tar"
    private const val FILE_PDF = "application/pdf"
    private const val FILE_MATROSKA = "application/x-matroska"
    private const val FILE_PEM = "application/pkix-cert+pem"
    private const val FILE_CERT = "application/pkix-cert"
    private const val FILE_SCRIPT = "application/x-shellscript"
    private const val FILE_FLASH = "application/vnd.adobe.flash.movie"
    private const val FILE_EXE = "application/vnd.microsoft.portable-executable"
    private const val FILE_CA_CERT = "application/x-x509-ca-cert"
    private const val FILE_ELF_EXE = "application/x-executable"
    private const val FILE_ELF_RE = "application/x-object"
    private const val FILE_ELF_SO = "application/x-sharedlib"
    private const val FILE_MS_EXE = "application/x-dosexec"
    private const val FILE_APL_EXE = "application/x-mach-binary"
    private const val FILE_TORRENT = "application/x-bittorrent"
    private const val FILE_ODT = "application/vnd.oasis.opendocument.text"
    private const val FILE_XRIFF = "application/x-riff" // +webp

    private const val EXT_APNG = ".apng"
    private const val EXT_PNG = ".png"
    private const val EXT_JPG = ".jpg"
    private const val EXT_JPEG = ".jpeg"
    private const val EXT_GIF = ".gif"
    private const val EXT_WEBP = ".webp"
    private const val EXT_SVG = ".svg"
    private const val EXT_APK = Const.DOT_APK
    private const val EXT_ZIP = ".zip"
    private const val EXT_XAPK = ".xapk"
    private const val EXT_APKS = ".apks"
    private const val EXT_APKM = ".apkm"
    private const val EXT_DEX = ".dex"
    private const val EXT_ODEX = ".odex"
    private const val EXT_VDEX = ".vdex"
    private const val EXT_TAR = ".tar"
    private const val EXT_BZ2 = ".bz2"
    private const val EXT_DMG = ".dmg"
    private const val EXT_GZ = ".gz"
    private const val EXT_RAR = ".rar"
    private const val EXT_TXT = ".txt"
    private const val EXT_INI = ".ini"
    private const val EXT_CPP = ".cpp"
    private const val EXT_INO = ".ino"
    private const val EXT_JAVA = ".java"
    private const val EXT_KT = ".kt"
    private const val EXT_KTS = ".kts"
    private const val EXT_SWIFT = ".swift"
    private const val EXT_YAML = ".yaml"
    private const val EXT_HTML = ".html"
    private const val EXT_SH = ".sh"
    private const val EXT_BAT = ".bat"
    private const val EXT_IMG = ".img"
    private const val EXT_MP4 = ".mp4"
    private const val EXT_MKV = ".mkv"
    private const val EXT_MOV = ".mov"
    private const val EXT_WEBM = ".webm"
    private const val EXT_3GP = ".3gp"
    private const val EXT_AVI = ".avi"
    private const val EXT_AVIF = ".avif"
    private const val EXT_MP3 = ".mp3"
    private const val EXT_M4A = ".m4a"
    private const val EXT_OGG = ".ogg"
    private const val EXT_WAV = ".wav"
    private const val EXT_SWF = ".swf"
    private const val EXT_FLAC = ".flac"
    private const val EXT_AAC = ".aac"
    private const val EXT_OGA = ".oga"
    private const val EXT_FAP = ".fap"
    private const val EXT_PDF = ".pdf"
    private const val EXT_PEM = ".pem"
    private const val EXT_P12 = ".p12"
    private const val EXT_CRT = ".crt"
    private const val EXT_TORRENT = ".torrent"
    private const val EXT_EXE = ".exe"
    private const val EXT_XPI = ".xpi" // Mozilla extension
    private const val EXT_OSZ = ".osz" // osu map
    private const val EXT_OSK = ".osk" // osu skin
    private const val EXT_OSU = ".osu" // osu beatmap level
    private const val EXT_OLZ = ".olz" // osu lazer map
    private const val EXT_OSR = ".osr" // osu replay
    private const val EXT_OSB = ".osb" // osu storyboard

    fun copy(from: Node, to: Node, asSu: Boolean): Node? {
        val result = NativeBridge.copy(from.ref, to.ref, asSu = asSu) {
            // todo
        }
        return from.apply(result)?.update(CacheConfig(asSu), ensureCached = false)
    }

    fun create(parent: Node, name: String, directory: Boolean, asSu: Boolean): Node? {
        val target = parent.ref + name
        val output = when {
            directory -> NativeBridge.createDir(target, asSu)
            else -> NativeBridge.createFile(target, asSu)
        }
        val content = when {
            directory -> NodeContent.Directory()
            else -> NodeContent.Empty
        }
        val meta = when (output) {
            is Rslt.Ok -> output.value
            is Rslt.Err -> return null
        }
        return Node(ref = target, parentRef = parent.ref, rootId = parent.rootId, properties = meta.toProperties(), content = content)
    }

    fun Node.Companion.asRoot(ref: NodeRef, type: NodeRootType): Node {
        return Node(
            ref = ref,
            properties = NodeProperties(),
            content = NodeContent.Directory(rootType = type),
        )
    }

    fun Meta.toProperties(size: String? = null) = NodeProperties(
        access = access,
        owner = owner,
        group = group,
        date = date,
        time = time,
        length = if (access.firstOrNull() == FILE_CHAR) length.toLong() else 0,
        size = this.size.takeIf { it.isNotEmpty() } ?: size ?: "",
    )

    private const val DIMENS = "BKMGTPEZYRQ"
    private fun Long.toSize(): String {
        when {
            this == 0L -> return "0B"
            this < 0L -> return "?B"
        }
        var dim = 0
        var tmp = this
        var secondary = 0L
        while (tmp >= 1024 && dim < DIMENS.lastIndex.dec()) {
            secondary = tmp % 1024
            tmp /= 1024
            dim++
        }
        val builder = StringBuilder()
        if (tmp <= 9 && secondary >= 950) {
            tmp++
            secondary = 0
        }
        builder.append(tmp)
        if (builder.length == 1 && secondary >= 50) {
            val dec = (secondary / 100f).roundToInt()
            if (dec > 0) {
                builder.append('.')
                builder.append(dec)
            }
        }
        builder.append(DIMENS[dim])
        return builder.toString()
    }

    private fun parse(ref: NodeRef, parentRef: NodeRef, root: Int, properties: NodeProperties): Node {
        val content = when (properties.access.firstOrNull()) {
            DIR_CHAR -> NodeContent.Directory(DirectoryKind.Ordinary)
            LINK_CHAR -> NodeContent.Link
            null -> NodeContent.Unknown
            else -> resolveFileType(ref)
        }
        return Node(
            rootId = root,
            ref = ref,
            parentRef = parentRef,
            properties = properties,
            content = content,
        )
    }

    fun getDirectoryType(name: String): DirectoryKind {
        return when (name) {
            "Alarms" -> DirectoryKind.Alarms
            "Android" -> DirectoryKind.Android
            "DCIM" -> DirectoryKind.Camera
            "Download" -> DirectoryKind.Download
            "Movies" -> DirectoryKind.Movies
            "Music" -> DirectoryKind.Music
            "Pictures" -> DirectoryKind.Pictures
            "Ringtones" -> DirectoryKind.Ringtones
            else -> DirectoryKind.Ordinary
        }
    }

    fun Node.update(config: CacheConfig, ensureCached: Boolean = true): Node {
        val type = NativeBridge.type(ref, config.asSu)
        return when (type) {
            is Rslt.Ok -> parseNode(type.value.meta).resolveType(type.value.mime)
                .run { if (ensureCached) ensureCached(config, oldProps = properties) else this }
            is Rslt.Err -> copy(error = type.message.toNodeError())
        }
    }

    private fun Node.ensureCached(config: CacheConfig, oldProps: NodeProperties): Node = when {
        isDirectory -> cacheDir(config.asSu)
        length == 0L && oldProps.size != size -> resolveFileType()
        length == 0L -> this
        isCached && oldProps.size == size -> this
        // if size changed -> cache again
        else -> try {
            cacheFile(config)
        } catch (e: Exception) {
            this.copy(error = NodeError.Message(e.toString()))
        }
    }

    private fun Node.cacheDir(asSu: Boolean): Node {
        val result = NativeBridge.metas(ref, asSu)
        return when (result) {
            is Rslt.Ok -> parseDir(result.value)
            is Rslt.Err -> copy(error = result.message.toNodeError())
        }
    }

    /** resolve content types */
    fun Node.resolveDirChildren(asSu: Boolean): Boolean {
        val children = children ?: return false
        val types = NativeBridge.types(ref, asSu)
        val entries = when (types) {
            is Rslt.Ok -> types.value
            is Rslt.Err -> return false
        }
        entries.forEach { entry ->
            val index = children.items
                .indexOfFirst { it.ref.theSame(entry.meta.path) }
                .also { if (it < 0) return@forEach }
            children.run {
                val child = items[index]
                items[index] = child.resolveType(mimeType = entry.mime)
                    .copy(properties = entry.meta.toProperties(child.size))
            }
        }
        return entries.isNotEmpty()
    }

    fun Node.resolveType(mimeType: String): Node {
        val content = when (true) {
            (access.firstOrNull() == DIR_CHAR),
            (mimeType == DIRECTORY),
            (content is NodeContent.Directory) -> content.ifNotCached { NodeContent.Directory() }
            (length == 0L) -> NodeContent.Empty
            mimeType.isBlank(),
            (mimeType == FILE_UNKNOWN) -> content.resolveFileType(ref)
            mimeType.startsWith(FILE_PICTURE) -> content.ifNotCached { NodeContent.Picture.resolve(mimeType) }
            (mimeType == FILE_XRIFF) -> content.ifNotCached { NodeContent.Picture(mimeType) }
            (mimeType == FILE_APK) -> content.ifNotCached { AndroidApp.apk(ref) }
            (mimeType == FILE_RAR) -> content.ifNotCached { NodeContent.Rar() }
            (mimeType == FILE_ZIP) -> when (true) {
                name.hasExt(EXT_APKS),
                name.hasExt(EXT_APKM) -> content.ifNotCached { AndroidApp.apks(ref) }
                (content is AndroidApp) -> return this
                name.hasExt(EXT_OSZ) -> content.ifNotCached { NodeContent.Osu.Map() }
                name.hasExt(EXT_OSK) -> content.ifNotCached { NodeContent.Osu.Skin() }
                name.hasExt(EXT_OLZ) -> content.ifNotCached { NodeContent.Osu.LazerMap() }
                name.hasExt(EXT_OSR) -> content.ifNotCached { NodeContent.Osu.Replay() }
                name.hasExt(EXT_OSB) -> content.ifNotCached { NodeContent.Osu.Storyboard() }
                else -> content.ifNotCached { NodeContent.Zip() }
            }
            (mimeType == FILE_BZIP2) -> when {
                name.hasExt(EXT_DMG) -> content.ifNotCached { NodeContent.Dmg }
                else -> content.ifNotCached { NodeContent.Bzip2() }
            }
            (mimeType == FILE_GZIP) -> content.ifNotCached { NodeContent.Gz() }
            (mimeType == FILE_TAR) -> content.ifNotCached { NodeContent.Tar() }
            (mimeType == FILE_XZ) -> content.ifNotCached { NodeContent.Xz }
            mimeType.startsWith(FILE_FLASH) -> content.ifNotCached { NodeContent.Flash }
            mimeType.startsWith(FILE_EXE) -> content.ifNotCached { NodeContent.ExeMs }
            mimeType.startsWith(FILE_MESSAGE) -> NodeContent.Text.Plain
            mimeType.startsWith(FILE_TEXT) -> when {
                name.hasExt(EXT_SVG) -> content.ifNotCached { NodeContent.Text.Svg }
                name.hasExt(EXT_OSU) -> content.ifNotCached { NodeContent.Text.Osu }
                name.hasExt(EXT_CPP) -> content.ifNotCached { NodeContent.Text.Cpp }
                name.hasExt(EXT_INO) -> content.ifNotCached { NodeContent.Text.Ino }
                name.hasExt(EXT_BAT) -> content.ifNotCached { NodeContent.Text.BatScript }
                else -> NodeContent.Text.Plain
            }
            (mimeType == FILE_XML) -> content.ifNotCached { NodeContent.Text.Xml }
            mimeType.startsWith(FILE_AUDIO) -> content.ifNotCached { NodeContent.Music.resolve(mimeType) }
            mimeType.startsWith(FILE_VIDEO),
            (mimeType == FILE_MATROSKA) -> content.ifNotCached { NodeContent.Movie.resolve(mimeType) }
            (mimeType == FILE_PDF) -> content.ifNotCached { NodeContent.Pdf }
            (mimeType == FILE_ELF_EXE) -> when {
                name.hasExt(EXT_ODEX) -> content.ifNotCached { NodeContent.Java }
                else -> content.ifNotCached { NodeContent.Elf }
            }
            (mimeType == FILE_ELF_RE) -> when {
                name.hasExt(EXT_FAP) -> content.ifNotCached { NodeContent.Fap }
                else -> content.ifNotCached { NodeContent.Elf }
            }
            (mimeType == FILE_PEM),
            (mimeType == FILE_CERT),
            (mimeType == FILE_CA_CERT) -> content.ifNotCached { NodeContent.Cert }
            (mimeType == FILE_TORRENT) -> content.ifNotCached { NodeContent.Torrent }
            (mimeType == FILE_ODT) -> content.ifNotCached { NodeContent.Document }
            (mimeType == FILE_ELF_SO) -> content.ifNotCached { NodeContent.ElfSo }
            (mimeType == FILE_MS_EXE) -> content.ifNotCached { NodeContent.ExeMs }
            (mimeType == FILE_APL_EXE) -> content.ifNotCached { NodeContent.ExeApl }
            (mimeType == FILE_JAVA) -> content.ifNotCached { NodeContent.Java }
            (mimeType == FILE_SCRIPT),
            mimeType.startsWith(FILE_TEXT_SCRIPT) -> NodeContent.Text.ShellScript
            else -> {
                logE("'${ref.ext}' unknown type: $mimeType ${ref.takeIfDebug()}")
                content.resolveFileType(ref)
            }
        }
        return copy(content = content)
    }

    private fun Node.cacheFile(config: CacheConfig): Node {
        val content = when (content) {
            is NodeContent.Picture,
            is NodeContent.Movie -> content
            is NodeContent.Music -> content.copy(thumbnail = ref.string.createAudioThumbnail(config)?.forNode)
            is NodeContent.Zip -> cacheZip().let { item ->
                when (children?.possibleContainsMainApk()) {
                    null, false -> return item
                    true -> AndroidApp.apks(ref).let { apks ->
                        apks.tryGetApksContent(ref.string)
                            .contentOrNodeError(this, apks.copy(isCached = true)) { return it }
                    }
                }
            }
            is AndroidApp -> when {
                content.splitApk -> content
                    .tryGetApksContent(ref.string)
                    .contentOrNodeError(this, content.copy(isCached = true)) { return it }
                else -> content
                    .getApkContent(ref.string)
                    .contentOrNodeError(this, content.copy(isCached = true)) { return it }
            }
            else -> return this
        }
        return copy(content = content)
    }

    private inline fun <C : NodeContent> Rslt<C>.contentOrNodeError(node: Node, content: NodeContent, action: (withError: Node) -> Nothing): C {
        return unwrapOrElse {
            action(node.copy(content = content, error = NodeError.Message.orUnknown(it)))
        }
    }

    private fun AndroidApp.tryGetApksContent(zipPath: String): Rslt<AndroidApp> = try {
        getApksContent(zipPath)
    } catch (e: Exception) {
        e.toRslt()
    }

    private fun Node.cacheZip(): Node = try {
        val children = mutableListOf<Node>()
        ZipInputStream(BufferedInputStream(FileInputStream(ref.string))).use { stream ->
            var entry: ZipEntry? = stream.nextEntry
            while (entry != null) {
                if (entry.name.isEmpty()) {
                    entry = stream.nextEntry
                    continue
                }
                val content = when {
                    entry.isDirectory -> NodeContent.Directory()
                    else -> NodeContent.Unknown
                }
                val dateTime = SimpleDateFormat(NodeProperties.DATE_TIME_FORMAT, Locale.ROOT)
                    .format(Date(entry.time))
                    .split(NodeProperties.DATE_TIME_SEPARATOR)
                val properties = NodeProperties(date = dateTime.first(), time = dateTime.last(), size = entry.size.toSize(), length = entry.size)
                val child = Node(ref + entry.name, parentRef = ref, rootId = uniqueId, properties = properties, content = content)
                children.add(child)
                entry = stream.nextEntry
            }
        }
        val content = (content as NodeContent.Zip).copy(isCached = true)
        copy(children = NodeChildren(children), content = content)
    } catch (e: Exception) {
        copy(error = NodeError.Message(e.forHumans()))
    }

    private inline fun <reified T : NodeContent> NodeContent?.ifNotCached(action: () -> T): T {
        return if (this !is T || !isCached) action() else this
    }

    fun Node.sortBy(how: NodeSorting): Node = when (how) {
        is NodeSorting.Size,
        is NodeSorting.Name -> sortByName(reversed = how.reversed)
        is NodeSorting.Date -> sortByDate(newFirst = how.reversed)
    }

    fun Node.sortByName(reversed: Boolean = false): Node {
        children?.update(updateMetadata = false) {
            sortBy { it.name.lowercase() }
            if (reversed) reverse()
            sortBy { !it.isDirectory }
        }
        return this
    }

    private fun Node.sortByDate(newFirst: Boolean = true): Node {
        children?.update(updateMetadata = false) {
            sortBy { it.time }
            sortBy { it.date }
            if (newFirst) reverse()
            sortBy { !it.isDirectory }
        }
        return this
    }

    private fun Node.parseNode(meta: Meta): Node {
        val properties = meta.toProperties(size)
        val (children, content) = when {
            properties.isDirectory() -> when (content) {
                is NodeContent.Directory -> children to content
                else -> null to NodeContent.Directory()
            }
            properties.isLink() -> when (content) {
                is NodeContent.Link -> children to content
                else -> null to NodeContent.Link
            }
            properties.isFile() -> when (content) {
                is NodeContent.File -> children to content
                else -> null to resolveFileType(ref)
            }
            else -> null to NodeContent.Undefined
        }
        return copy(children = children, properties = properties, content = content)
    }

    private fun Node.parseDir(metas: List<Meta>): Node {
        val items = MutableList<Node>(metas.size)
        val files = MutableList<Node>(metas.size)
        for (i in metas.indices) {
            val meta = metas[i]
            var properties = meta.toProperties()
            val parentRef = this@parseDir.ref
            val ref = NodeRef(meta.path)
            val child = children?.findOnMut { it.ref == ref }
            if (child?.isDirectory == true) {
                properties = properties.copy(size = child.properties.size)
            }
            val item = when {
                child == null -> parse(ref, parentRef, rootId, properties)
                child.properties == properties -> child
                else -> child.copy(properties = properties)
            }
            when {
                item.isDirectory -> items.add(item)
                item.content is NodeContent.Undefined -> files.add(item.copy(content = resolveFileType(item.ref)))
                else -> files.add(item)
            }
        }
        items.addAll(files)
        val directoryKind = when (content) {
            is NodeContent.Directory -> content.kind
            else -> DirectoryKind.Ordinary
        }
        return copy(
            children = NodeChildren(items),
            content = NodeContent.Directory(directoryKind, content.rootType),
            error = null,
        )
    }

    fun Node.isParentOf(other: Node): Boolean = other.parentRef == ref

    fun Node.isSomeParentOf(other: Node): Boolean = ref.length <= other.ref.length && other.ref.isChildOf(ref)

    fun NodeProperties.isFile(): Boolean = access.firstOrNull() == FILE_CHAR

    fun NodeProperties.isDirectory(): Boolean = access.firstOrNull() == DIR_CHAR

    fun NodeProperties.isLink(): Boolean = access.firstOrNull() == LINK_CHAR

    fun NodeRef.isContent() = string.startsWith(Const.SCHEME_CONTENT)

    // means this node the fake, may be is a visual separating item, isn't a dir
    fun Node.isSeparator(): Boolean = ref.uniqueId == -uniqueId

    fun Node.asSeparator(): Node = when {
        isSeparator() -> this.also { debugFail { "is already separator" } }
        else -> copy(uniqueId = -uniqueId)
    }

    fun Node.delete(asSu: Boolean): Node? {
        val result = NativeBridge.delete(ref, asSu)
        return apply(result)
    }

    fun Node.rename(name: String, asSu: Boolean): Node? {
        val targetRef = parentRef + name
        val result = NativeBridge.copy(from = ref, to = targetRef, asSu = asSu, move = true) {
            // todo
        }
        return apply(result)
    }

    fun Node.apply(result: ComplexResult): Node? {
        val meta = when (result) {
            is ComplexResult.Ok -> result.meta
            is ComplexResult.Err -> result.v1
        }
        meta ?: return null
        var ref = NodeRef(meta.path)
        val newRef = ref != this.ref
        if (!newRef) ref = this.ref
        val properties = meta.toProperties(size.takeIf { !newRef })
        return copy(ref = ref, parentRef = ref.parent, uniqueId = ref.uniqueId, properties = properties, error = meta.error?.toNodeError())
    }

    fun Node.move(parent: NodeRef = parentRef, name: String = this.name): Node {
        val ref = parent + name
        children?.move(ref)
        return copy(ref = ref, parentRef = parent, uniqueId = ref.uniqueId, properties = properties, state = stateStub)
    }

    private fun NodeChildren.move(parent: NodeRef) {
        for (i in indices) {
            items[i] = get(i).move(parent = parent)
        }
    }

    private fun String.toNodeError(): NodeError {
        val lines = split(LF).filter { it.isNotBlank() }
        val first = lines.firstOrNull()
        return when {
            lines.size > 1 -> NodeError.Multiply(lines)
            first == null -> NodeError.Unknown
            first.startsWith(LS_NO_SUCH_FILE) -> NodeError.NoSuchFile
            first.startsWith(LS_PERMISSION_DENIED) -> NodeError.PermissionDenied
            else -> NodeError.Message.orUnknown(first)
        }
    }

    fun NodeStateImpl?.theSame(cachingJob: Job?, operation: NodeOperation): Boolean {
        val currentOperation = this?.operation ?: NodeOperation.None
        return when {
            this?.cachingJob != cachingJob -> false
            currentOperation != operation -> false
            else -> true
        }
    }

    private fun Node.resolveFileType(): Node {
        val currentOrNull = content.takeIf { !isCached || length > 0L }
        val new = currentOrNull.resolveFileType(ref)
        return if (new == content) this else copy(content = new)
    }

    private fun resolveFileType(ref: NodeRef) = null.resolveFileType(ref)

    private fun NodeContent?.resolveFileType(ref: NodeRef): NodeContent = when (true) {
        ref.name.hasExt(EXT_APNG) -> ifNotCached { NodeContent.Picture.Apng }
        ref.name.hasExt(EXT_PNG) -> ifNotCached { NodeContent.Picture.Png }
        ref.name.hasExt(EXT_JPG),
        ref.name.hasExt(EXT_JPEG) -> ifNotCached { NodeContent.Picture.Jpeg }
        ref.name.hasExt(EXT_GIF) -> ifNotCached { NodeContent.Picture.Gif }
        ref.name.hasExt(EXT_WEBP) -> ifNotCached { NodeContent.Picture.Webp }
        ref.name.hasExt(EXT_AVIF) -> ifNotCached { NodeContent.Picture.Avif }
        ref.name.hasExt(EXT_APK) -> ifNotCached { AndroidApp.apk(ref) }
        ref.name.hasExt(EXT_DEX),
        ref.name.hasExt(EXT_ODEX),
        ref.name.hasExt(EXT_VDEX) -> ifNotCached { NodeContent.Java }
        ref.name.hasExt(EXT_APKS),
        ref.name.hasExt(EXT_APKM) -> ifNotCached { AndroidApp.apks(ref) }
        ref.name.hasExt(EXT_ZIP),
        ref.name.hasExt(EXT_XAPK) -> ifNotCached { NodeContent.Zip() }
        ref.name.hasExt(EXT_TAR) -> ifNotCached { NodeContent.Tar() }
        ref.name.hasExt(EXT_BZ2) -> ifNotCached { NodeContent.Bzip2() }
        ref.name.hasExt(EXT_GZ) -> ifNotCached { NodeContent.Gz() }
        ref.name.hasExt(EXT_RAR) -> ifNotCached { NodeContent.Rar() }
        ref.name.hasExt(EXT_SH) -> NodeContent.Text.ShellScript
        ref.name.hasExt(EXT_BAT) -> NodeContent.Text.BatScript
        ref.name.hasExt(EXT_TXT),
        ref.name.hasExt(EXT_INI),
        ref.name.hasExt(EXT_JAVA),
        ref.name.hasExt(EXT_KT),
        ref.name.hasExt(EXT_KTS),
        ref.name.hasExt(EXT_SWIFT),
        ref.name.hasExt(EXT_YAML),
        ref.name.hasExt(EXT_HTML) -> NodeContent.Text.Plain
        ref.name.hasExt(EXT_SVG) -> ifNotCached { NodeContent.Text.Svg }
        ref.name.hasExt(EXT_IMG) -> NodeContent.DataImage
        ref.name.hasExt(EXT_MP4) -> ifNotCached { NodeContent.Movie.Mp4 }
        ref.name.hasExt(EXT_MKV) -> ifNotCached { NodeContent.Movie.Mkv }
        ref.name.hasExt(EXT_MOV) -> ifNotCached { NodeContent.Movie.Mov }
        ref.name.hasExt(EXT_WEBM) -> ifNotCached { NodeContent.Movie.Webm }
        ref.name.hasExt(EXT_3GP) -> ifNotCached { NodeContent.Movie.Tgp }
        ref.name.hasExt(EXT_AVI) -> ifNotCached { NodeContent.Movie.Avi }
        ref.name.hasExt(EXT_MP3) -> ifNotCached { NodeContent.Music.Mp3 }
        ref.name.hasExt(EXT_M4A) -> ifNotCached { NodeContent.Music.M4a }
        ref.name.hasExt(EXT_OGA),
        ref.name.hasExt(EXT_OGG) -> ifNotCached { NodeContent.Music.Ogg }
        ref.name.hasExt(EXT_WAV) -> ifNotCached { NodeContent.Music.Wav }
        ref.name.hasExt(EXT_FLAC) -> ifNotCached { NodeContent.Music.Flac }
        ref.name.hasExt(EXT_AAC) -> ifNotCached { NodeContent.Music.Aac }
        ref.name.hasExt(EXT_PDF) -> ifNotCached { NodeContent.Pdf }
        ref.name.hasExt(EXT_TORRENT) -> ifNotCached { NodeContent.Torrent }
        ref.name.hasExt(EXT_FAP) -> ifNotCached { NodeContent.Fap }
        ref.name.hasExt(EXT_EXE) -> ifNotCached { NodeContent.ExeMs }
        ref.name.hasExt(EXT_SWF) -> ifNotCached { NodeContent.Flash }
        ref.name.hasExt(EXT_PEM),
        ref.name.hasExt(EXT_P12),
        ref.name.hasExt(EXT_CRT) -> ifNotCached { NodeContent.Cert }
        ref.name.hasExt(EXT_OSZ) -> ifNotCached { NodeContent.Osu.Map() }
        ref.name.hasExt(EXT_OSK) -> ifNotCached { NodeContent.Osu.Skin() }
        ref.name.hasExt(EXT_OLZ) -> ifNotCached { NodeContent.Osu.LazerMap() }
        ref.name.hasExt(EXT_OSR) -> ifNotCached { NodeContent.Osu.Replay() }
        ref.name.hasExt(EXT_OSB) -> ifNotCached { NodeContent.Osu.Storyboard() }
        ref.name.hasExt(EXT_XPI) -> ifNotCached { NodeContent.Firefox }
        else -> NodeContent.Other
    }

    fun Node.updateWith(item: Node, sorting: NodeSorting? = null): Node {
        val children = when {
            children == null && item.children == null -> null
            children == null || item.children == null -> item.children
            children === item.children -> children
            children.items === item.children.items -> children // fixes ConcurrentModificationException
            else -> item.children.also { newChildren ->
                val iterator = newChildren.items.listIterator()
                var oldIndex = 0
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    var old = children.getOrNull(oldIndex)
                    if (old?.uniqueId != next.uniqueId) {
                        val index = children.indexOfFirst { it.uniqueId == next.uniqueId }
                        if (index >= 0) {
                            old = children[index]
                            oldIndex = index
                        } else {
                            continue
                        }
                    }
                    oldIndex++
                    val actual = when (old.properties) {
                        next.properties -> old
                        else -> old.copy(properties = next.properties)
                    }
                    iterator.set(actual)
                }
            }
        }
        val content = when (true) {
            (item.content::class != content::class),
            item.content.isCached -> item.content
            content.isCached -> content
            else -> content
        }
        return copy(
            properties = item.properties,
            content = content,
            children = children,
            error = item.error,
        ).run {
            sortBy(sorting ?: return@run this)
        }
    }

    fun Node.updateWith(new: NodeContent, properties: NodeProperties): Node {
        val content = when (true) {
            (new::class != content::class),
            !isCached -> new
            else -> null
        }
        val properties = when {
            properties == this.properties -> null
            !properties.isDirectory() -> properties
            properties.size.isNotEmpty() -> properties
            this.properties.size.isEmpty() -> properties
            else -> properties.copy(size = this.properties.size)
        }
        return when (true) {
            (properties != null),
            (content != null) -> copy(content = content ?: this.content, properties = properties ?: this.properties)
            else -> this
        }
    }

    fun List<Node>.merge() = toMutableList().apply {
        var i = 0
        var j = 1
        while (i < lastIndex) {
            val first = get(i)
            val second = get(j)
            when {
                first.isSomeParentOf(second) -> removeAt(j)
                second.isSomeParentOf(first) -> removeAt(i)
                j == lastIndex -> j = ++i + 1
                else -> j++
            }
        }
    }

    private fun String.hasExt(ext: String) = endsWith(ext, ignoreCase = true)
}
