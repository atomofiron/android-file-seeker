package app.atomofiron.searchboxapp.utils

import android.content.pm.PackageManager
import app.atomofiron.common.util.MutableList
import app.atomofiron.common.util.extension.logE
import app.atomofiron.common.util.extension.takeIfDebug
import app.atomofiron.common.util.property.MutableWeakProperty
import app.atomofiron.searchboxapp.android.NativeBridge
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.DirectoryKind
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.Node.Companion.stateStub
import app.atomofiron.searchboxapp.model.explorer.Node.Companion.toUniqueId
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeContent.AndroidApp
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeOperation
import app.atomofiron.searchboxapp.model.explorer.NodeProperties
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.explorer.NodeState
import app.atomofiron.searchboxapp.model.explorer.other.forNode
import app.atomofiron.searchboxapp.utils.Const.LF
import app.atomofiron.searchboxapp.utils.Const.SLASH
import bridge.Bridge
import kotlinx.coroutines.Job
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

    private const val ROOT_PARENT_PATH = "root_parent_path"

    private const val ROOT = SLASH.toString()
    private const val DIR_CHAR = 'd'
    private const val LINK_CHAR = 'l'
    private const val FILE_CHAR = '-'
    private const val LS_NO_SUCH_FILE = "No such file or directory"
    private const val LS_PERMISSION_DENIED = "Permission denied"
    private const val COMMAND_PATH_PREFIX = "[a-z]+: %s: "

    private const val DIRECTORY = "inode/directory"
    private const val FILE_UNKNOWN = "application/octet-stream"
    private const val FILE_PICTURE = "image/"
    private const val FILE_ZIP = "application/zip"
    private const val FILE_GZIP = "application/gzip"
    private const val FILE_JAVA = "application/java-vm"
    private const val FILE_XZ = "application/x-xz"
    private const val FILE_BZIP2 = "application/x-bzip2"
    private const val FILE_TAR = "application/x-tar"
    private const val FILE_TEXT = "text/plain"
    private const val FILE_SCRIPT = "text/x-"
    private const val FILE_PDF = "application/pdf"
    private const val FILE_AUDIO = "audio/"
    private const val FILE_VIDEO = "video/"
    private const val FILE_MATROSKA = "application/x-matroska"
    private const val FILE_PEM = "application/pkix-cert+pem"
    private const val FILE_CERT = "application/pkix-cert"
    private const val FILE_CA_CERT = "application/x-x509-ca-cert"
    private const val FILE_ELF_EXE = "application/x-executable"
    private const val FILE_ELF_RE = "application/x-object"
    private const val FILE_ELF_SO = "application/x-sharedlib"
    private const val FILE_MSP_EXE = "application/x-dosexec"
    private const val FILE_MS_EXE = "application/x-dosexec"
    private const val FILE_APL_EXE = "application/x-mach-binary"
    private const val FILE_APLS_EXE = "application/x-mach-binary"
    private const val FILE_TORRENT = "application/x-bittorrent"
    private const val FILE_ODT = "application/vnd.oasis.opendocument.text"

    private const val EXT_APNG = ".apng"
    private const val EXT_PNG = ".png"
    private const val EXT_JPG = ".jpg"
    private const val EXT_JPEG = ".jpeg"
    private const val EXT_GIF = ".gif"
    private const val EXT_WEBP = ".webp"
    private const val EXT_SVG = ".svg"
    private const val EXT_APK = ".apk"
    private const val EXT_ZIP = ".zip"
    private const val EXT_XAPK = ".xapk"
    private const val EXT_APKS = ".apks"
    private const val EXT_APKM = ".apkm"
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

    private val lastPart = Regex("(?<=/)/*[^/]+/*$|^/+$")

    fun String.completePath(directory: Boolean): String = when {
        this == ROOT -> this
        !directory -> this
        lastOrNull() == SLASH -> this
        isNotEmpty() -> "$this/"
        else -> throw IllegalStateException(this)
    }

    fun String.parent(): String = replace(lastPart, "")

    fun String.name(): String {
        if (isEmpty()) {
            return this
        }
        var nonSlash = false
        var end = length
        for (i in indices.reversed()) {
            if (nonSlash && get(i) == SLASH) {
                return substring(i.inc(), end)
            }
            if (!nonSlash && get(i) != SLASH) {
                nonSlash = true
                end = i.inc()
            }
        }
        return when {
            nonSlash -> substring(0, end)
            else -> SLASH.toString()
        }
    }

    fun copy(from: Node, to: Node, asSu: Boolean): Node {
        val output = Shell.exec(Shell[Shell.COPY].format(from.path, to.path), asSu)
        val new = to.update(CacheConfig(asSu), ensureCached = false)
        return when {
            output.success -> new
            else -> from.copy(error = output.error.toNodeError(from.path))
        }
    }

    fun create(parent: Node, name: String, directory: Boolean, asSu: Boolean): Node? {
        var targetPath = parent.path + name
        if (directory) {
            targetPath = targetPath.completePath(directory = true)
        }
        val output = when {
            directory -> NativeBridge.createDir(targetPath, asSu)
            else -> NativeBridge.createFile(targetPath, asSu)
        }
        val content = when {
            directory -> NodeContent.Directory()
            else -> NodeContent.Empty
        }
        val meta = when (output) {
            is Rslt.Ok -> output.data
            is Rslt.Err -> return null
        }
        return Node(path = targetPath, parentPath = parent.path, rootId = parent.rootId, properties = meta.toProperties(), content = content)
    }

    fun Node.Companion.asRoot(path: String, type: NodeRoot.NodeRootType): Node {
        return Node(
            path = path,
            parentPath = ROOT_PARENT_PATH,
            properties = NodeProperties(name = path.name()),
            content = NodeContent.Directory(rootType = type),
        )
    }

    private fun Bridge.Meta.toProperties(
        name: String = this.name,
        size: String = "",
    ): NodeProperties {
        val isFile = access.firstOrNull() == FILE_CHAR
        return NodeProperties(
            name = name,
            access = access,
            owner = owner,
            group = group,
            date = date,
            time = time,
            length = if (!isFile) 0 else length,
            size = if (!isFile) size else this.size,
        )
    }

    const val DIMENS = "BKMGTPEZYRQ"
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

    private fun parse(parentPath: String, root: Int, properties: NodeProperties): Node {
        val incompletePath = parentPath + properties.name
        val content = when (properties.access[0]) {
            DIR_CHAR -> NodeContent.Directory(DirectoryKind.Ordinary)
            LINK_CHAR -> NodeContent.Link
            else -> resolveFileType(incompletePath)
        }
        val asDir = content is NodeContent.Directory
        return Node(
            rootId = root,
            path = incompletePath.completePath(asDir),
            parentPath = parentPath,
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
        return when (val type = NativeBridge.type(path, config.asSu)) {
            is Rslt.Ok -> parseNode(type.data.meta).resolveType(type.data.mime)
                .run { if (ensureCached) ensureCached(config, oldProps = properties) else this }
            is Rslt.Err -> copy(error = type.message.toNodeError(path))
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
        val result = NativeBridge.metas(path, asSu)
        return when (result) {
            is Rslt.Ok -> parseDir(result.data)
            is Rslt.Err -> copy(error = result.message.toNodeError(path))
        }
    }

    /** resolve content types */
    fun Node.resolveDirChildren(asSu: Boolean): Boolean {
        val children = children ?: return false
        val types = NativeBridge.types(path, asSu)
        val entries = when (types) {
            is Rslt.Ok -> types.data
            is Rslt.Err -> return false
        }
        entries.forEach { entry ->
            val index = children.items
                .indexOfFirst { it.name == entry.meta.name }
                .also { if (it < 0) return@forEach }
            children.run {
                items[index] = items[index].resolveType(mimeType = entry.mime)
                    .copy(properties = entry.meta.toProperties())
            }
        }
        return entries.isNotEmpty()
    }

    fun Node.resolveSize(asSu: Boolean): String = Shell.exec(Shell[Shell.DU_HD0].format(path), asSu)
        .output
        .split(Const.TAB)
        .takeIf { it.size == 2 }
        ?.firstOrNull()
        ?.replace(".0", "")
        ?: ""

    private fun Node.resolveType(mimeType: String): Node {
        val content = when (true) {
            (access.firstOrNull() == DIR_CHAR),
            mimeType.startsWith(DIRECTORY),
            (content is NodeContent.Directory) -> content.ifNotCached { NodeContent.Directory() }
            (length == 0L) -> NodeContent.Empty
            mimeType.isBlank(),
            (mimeType == FILE_UNKNOWN) -> content.resolveFileType(path)
            mimeType.startsWith(FILE_PICTURE) -> content.ifNotCached { NodeContent.Picture(path, mimeType) }
            mimeType.startsWith(FILE_ZIP) -> when (true) {
                path.endsWith(EXT_APK, ignoreCase = true) -> content.ifNotCached { AndroidApp.apk(path) }
                path.endsWith(EXT_APKS, ignoreCase = true),
                path.endsWith(EXT_APKM, ignoreCase = true) -> content.ifNotCached { AndroidApp.apks(path) }
                (content is AndroidApp) -> return this
                path.endsWith(EXT_OSZ, ignoreCase = true) -> content.ifNotCached { NodeContent.Osu.Map() }
                path.endsWith(EXT_OSK, ignoreCase = true) -> content.ifNotCached { NodeContent.Osu.Skin() }
                path.endsWith(EXT_OLZ, ignoreCase = true) -> content.ifNotCached { NodeContent.Osu.LazerMap() }
                path.endsWith(EXT_OSR, ignoreCase = true) -> content.ifNotCached { NodeContent.Osu.Replay() }
                path.endsWith(EXT_OSB, ignoreCase = true) -> content.ifNotCached { NodeContent.Osu.Storyboard() }
                else -> content.ifNotCached { NodeContent.Zip() }
            }
            mimeType.startsWith(FILE_BZIP2) -> when {
                name.endsWith(EXT_DMG) -> content.ifNotCached { NodeContent.Dmg }
                else -> content.ifNotCached { NodeContent.Bzip2() }
            }
            mimeType.startsWith(FILE_GZIP) -> content.ifNotCached { NodeContent.Gz() }
            mimeType.startsWith(FILE_TAR) -> content.ifNotCached { NodeContent.Tar() }
            mimeType.startsWith(FILE_XZ) -> content.ifNotCached { NodeContent.Xz }
            mimeType.startsWith(FILE_TEXT) -> when {
                path.endsWith(EXT_SVG, ignoreCase = true) -> content.ifNotCached { NodeContent.Text.Svg }
                path.endsWith(EXT_OSU, ignoreCase = true) -> content.ifNotCached { NodeContent.Text.Osu }
                path.endsWith(EXT_CPP, ignoreCase = true) -> content.ifNotCached { NodeContent.Text.Cpp }
                path.endsWith(EXT_INO, ignoreCase = true) -> content.ifNotCached { NodeContent.Text.Ino }
                path.endsWith(EXT_BAT, ignoreCase = true) -> content.ifNotCached { NodeContent.Text.BatScript }
                else -> NodeContent.Text.Plain
            }
            mimeType.startsWith(FILE_AUDIO) -> content.ifNotCached { NodeContent.Music() }
            mimeType.startsWith(FILE_VIDEO),
            mimeType.startsWith(FILE_MATROSKA) -> content.ifNotCached { NodeContent.Movie(path) }
            mimeType.startsWith(FILE_PDF) -> content.ifNotCached { NodeContent.Pdf }
            mimeType.startsWith(FILE_ELF_EXE) -> content.ifNotCached { NodeContent.Elf }
            mimeType.startsWith(FILE_ELF_RE) -> when {
                name.endsWith(EXT_FAP) -> content.ifNotCached { NodeContent.Fap }
                else -> content.ifNotCached { NodeContent.Elf }
            }
            mimeType.startsWith(FILE_PEM),
            mimeType.startsWith(FILE_CERT),
            mimeType.startsWith(FILE_CA_CERT) -> content.ifNotCached { NodeContent.Cert }
            mimeType.startsWith(FILE_TORRENT) -> content.ifNotCached { NodeContent.Torrent }
            mimeType.startsWith(FILE_ODT) -> content.ifNotCached { NodeContent.Document }
            mimeType.startsWith(FILE_ELF_SO) -> content.ifNotCached { NodeContent.ElfSo }
            mimeType.startsWith(FILE_MSP_EXE),
            mimeType.startsWith(FILE_MS_EXE) -> content.ifNotCached { NodeContent.ExeMs }
            mimeType.startsWith(FILE_APLS_EXE) -> content.ifNotCached { NodeContent.ExeApls }
            mimeType.startsWith(FILE_APL_EXE) -> content.ifNotCached { NodeContent.ExeApl }
            mimeType.startsWith(FILE_JAVA) -> content.ifNotCached { NodeContent.Java }
            mimeType.startsWith(FILE_SCRIPT) -> NodeContent.Text.ShellScript
            else -> {
                val ext = name.lastIndexOf(Const.DOT).inc()
                    .let { if (it == 0) name.length else it }
                    .let { name.substring(it) }
                logE("'$ext' unknown type: $mimeType ${path.takeIfDebug()}")
                content.resolveFileType(path)
            }
        }
        return copy(content = content)
    }

    private fun Node.cacheFile(config: CacheConfig): Node {
        val content = when (content) {
            is NodeContent.Picture,
            is NodeContent.Movie -> content
            is NodeContent.Music -> NodeContent.Music(path.createAudioThumbnail(config)?.forNode, 0)
            is NodeContent.Zip -> cache(content).contentOrNodeError(this) { return it }.let { zip ->
                when (zip.children?.any { it.name == BASE_APK }) {
                    null, false -> zip
                    true -> AndroidApp.apks(path, children = zip.children).let { apks ->
                        apks.tryGetApksContent(path)
                            .contentOrNodeError(this, apks) { return it }
                    }
                }
            }
            is AndroidApp -> when {
                content.splitApk -> content
                    .tryGetApksContent(path)
                    .contentOrNodeError(this) { return it }
                else -> content
                    .getApkContent(path)
                    .contentOrNodeError(this) { return it }
            }
            else -> return this
        }
        return copy(content = content)
    }

    private inline fun <C : NodeContent> Rslt<C>.contentOrNodeError(node: Node, content: NodeContent? = null, action: (withError: Node) -> Nothing): C {
        return unwrapOrElse {
            action(node.copy(content = content ?: node.content, error = NodeError.Message(it)))
        }
    }

    private fun AndroidApp.tryGetApksContent(zipPath: String): Rslt<AndroidApp> = try {
        getApksContent(zipPath)
    } catch (e: Exception) {
        e.toRslt()
    }

    private fun Node.cache(content: NodeContent.Zip): Rslt<NodeContent.Zip> = try {
        val children = mutableListOf<Node>()
        ZipInputStream(BufferedInputStream(FileInputStream(path))).use { stream ->
            var entry: ZipEntry? = stream.nextEntry
            while (entry != null) {
                val new = when {
                    entry.isDirectory -> NodeContent.Directory()
                    else -> resolveFileType(entry.name)
                }
                val dateTime = SimpleDateFormat(NodeProperties.DATE_TIME_FORMAT, Locale.ROOT)
                    .format(Date(entry.time))
                    .split(NodeProperties.DATE_TIME_SEPARATOR)
                val properties = NodeProperties(name = entry.name, date = dateTime.first(), time = dateTime.last(), size = entry.size.toSize(), length = entry.size)
                val node = Node("$path/${entry.name}", parentPath = path, rootId = uniqueId, properties = properties, content = new)
                children.add(node)
                entry = stream.nextEntry
            }
        }
        content.copy(children = children).toRslt()
    } catch (e: Exception) {
        e.toRslt()
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

    private fun Node.parseNode(meta: Bridge.Meta): Node {
        val properties = meta.toProperties()
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
                else -> null to resolveFileType(path)
            }
            else -> null to NodeContent.Undefined
        }
        return copy(children = children, properties = properties, content = content)
    }

    private fun Node.parseDir(metas: List<Bridge.Meta>): Node {
        val items = MutableList<Node>(metas.size)
        val files = MutableList<Node>(metas.size)
        for (i in metas.indices) {
            val meta = metas[i]
            var properties = meta.toProperties()
            val child = children?.findOnMut { it.name == properties.name }
            if (child?.isDirectory == true) {
                properties = properties.copy(size = child.properties.size)
            }
            val item = when {
                child == null -> parse(path, rootId, properties)
                child.properties == properties -> child
                else -> child.copy(properties = properties)
            }
            when {
                item.isDirectory -> items.add(item)
                item.content is NodeContent.Undefined -> files.add(item.copy(content = resolveFileType(item.path)))
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

    fun Node.isParentOf(other: Node): Boolean = other.parentPath == path

    fun Node.isSomeParentOf(other: Node): Boolean = path.length <= other.path.length && other.path.startsWith(path)

    fun NodeChildren.clearChildren() = update {
        val iter = listIterator()
        while (iter.hasNext()) {
            val item = iter.next()
            when {
                item.error is NodeError.NoSuchFile -> iter.remove()
                item.isCached -> iter.set(item.copy(children = null))
            }
        }
    }

    fun NodeChildren?.areChildrenContentsTheSame(other: NodeChildren?): Boolean {
        when {
            other == null && this == null -> return true
            other == null -> return false
            this == null -> return false
            other.size != this.size -> return false
        }
        other.forEachIndexed { i, it ->
            if (!it.areContentsTheSame(get(i))) {
                return false
            }
        }
        return true
    }

    fun NodeProperties.isFile(): Boolean = access.firstOrNull() == FILE_CHAR

    fun NodeProperties.isDirectory(): Boolean = access.firstOrNull() == DIR_CHAR

    fun NodeProperties.isLink(): Boolean = access.firstOrNull() == LINK_CHAR

    // means this node the fake, may be is a visual separating item, isn't a dir
    fun Node.isSeparator(): Boolean = path.endsWith("/.")

    fun Node.asSeparator(): Node = when {
        isSeparator() -> this
        else -> copy(path = "$path.", uniqueId = -uniqueId)
    }

    fun Node.originalPath(): String = when {
        !isSeparator() -> throw UnsupportedOperationException()
        else -> path.substring(0, path.length.dec())
    }

    fun Node.withoutDot(): String = when {
        isSeparator() -> path.substring(0, path.length.dec())
        else -> path
    }

    fun Node.delete(asSu: Boolean): Node? {
        val result = NativeBridge.delete(path, asSu)
        val error = when (result) {
            is Rslt.Ok -> NodeError.Unknown.takeIf { !result.data }
            is Rslt.Err -> result.message.toNodeError(path)
        }
        return copy(error = error)
    }

    fun Node.rename(name: String, asSu: Boolean): Node {
        val targetPath = parentPath + name
        val output = Shell.exec(Shell[Shell.MV].format(path, targetPath), asSu)
        return when {
            output.success -> move(name = name).copy(error = null)
            else -> copy(error = output.error.toNodeError(path))
        }
    }

    fun Node.move(parent: String = parentPath, name: String = this.name): Node {
        val path = "$parent$name".completePath(isDirectory)
        val properties = if (name == this.name) properties else properties.copy(name = name)
        children?.move(path)
        return copy(path = path, parentPath = parent, uniqueId = path.toUniqueId(), properties = properties, state = stateStub)
    }

    private fun NodeChildren.move(parent: String) {
        for (i in indices) {
            items[i] = get(i).move(parent = parent.completePath(true))
        }
    }

    private fun String.toNodeError(path: String): NodeError {
        val lines = split(LF)
        val first = lines.find { it.isNotBlank() }
        return when {
            lines.size > 1 -> NodeError.Multiply(lines)
            first.isNullOrBlank() -> NodeError.Unknown
            first.startsWith(LS_NO_SUCH_FILE) -> NodeError.NoSuchFile
            first.startsWith(LS_PERMISSION_DENIED) -> NodeError.PermissionDenied
            else -> NodeError.Message(first.replace(Regex(COMMAND_PATH_PREFIX.format(path)), ""))
        }
    }

    fun NodeState?.theSame(cachingJob: Job?, operation: NodeOperation): Boolean {
        val currentOperation = this?.operation ?: NodeOperation.None
        return when {
            this?.cachingJob != cachingJob -> false
            currentOperation != operation -> false
            else -> true
        }
    }

    private fun Node.resolveFileType(): Node {
        val currentOrNull = content.takeIf { !isCached || length > 0L }
        val new = currentOrNull.resolveFileType(path)
        return if (new == content) this else copy(content = new)
    }

    private fun resolveFileType(path: String) = null.resolveFileType(path)

    private fun NodeContent?.resolveFileType(path: String): NodeContent = when (true) {
        path.endsWith(EXT_APNG, ignoreCase = true) -> ifNotCached { NodeContent.Picture.apng(path) }
        path.endsWith(EXT_PNG, ignoreCase = true) -> ifNotCached { NodeContent.Picture.png(path) }
        path.endsWith(EXT_JPG, ignoreCase = true),
        path.endsWith(EXT_JPEG, ignoreCase = true) -> ifNotCached { NodeContent.Picture.jpeg(path) }
        path.endsWith(EXT_GIF, ignoreCase = true) -> ifNotCached { NodeContent.Picture.gif(path) }
        path.endsWith(EXT_WEBP, ignoreCase = true) -> ifNotCached { NodeContent.Picture.webp(path) }
        path.endsWith(EXT_AVIF, ignoreCase = true) -> ifNotCached { NodeContent.Picture.avif(path) }
        path.endsWith(EXT_APK, ignoreCase = true) -> ifNotCached { AndroidApp.apk(path) }
        path.endsWith(EXT_APKS, ignoreCase = true),
        path.endsWith(EXT_APKM, ignoreCase = true) -> ifNotCached { AndroidApp.apks(path) }
        path.endsWith(EXT_ZIP, ignoreCase = true),
        path.endsWith(EXT_XAPK, ignoreCase = true) -> ifNotCached { NodeContent.Zip() }
        path.endsWith(EXT_TAR, ignoreCase = true) -> ifNotCached { NodeContent.Tar() }
        path.endsWith(EXT_BZ2, ignoreCase = true) -> ifNotCached { NodeContent.Bzip2() }
        path.endsWith(EXT_GZ, ignoreCase = true) -> ifNotCached { NodeContent.Gz() }
        path.endsWith(EXT_RAR, ignoreCase = true) -> ifNotCached { NodeContent.Rar() }
        path.endsWith(EXT_SH, ignoreCase = true) -> NodeContent.Text.ShellScript
        path.endsWith(EXT_BAT, ignoreCase = true) -> NodeContent.Text.BatScript
        path.endsWith(EXT_TXT, ignoreCase = true),
        path.endsWith(EXT_INI, ignoreCase = true),
        path.endsWith(EXT_JAVA, ignoreCase = true),
        path.endsWith(EXT_KT, ignoreCase = true),
        path.endsWith(EXT_KTS, ignoreCase = true),
        path.endsWith(EXT_SWIFT, ignoreCase = true),
        path.endsWith(EXT_YAML, ignoreCase = true),
        path.endsWith(EXT_HTML, ignoreCase = true) -> NodeContent.Text.Plain
        path.endsWith(EXT_SVG, ignoreCase = true) -> ifNotCached { NodeContent.Text.Svg }
        path.endsWith(EXT_IMG, ignoreCase = true) -> NodeContent.DataImage
        path.endsWith(EXT_MP4, ignoreCase = true),
        path.endsWith(EXT_MKV, ignoreCase = true),
        path.endsWith(EXT_MOV, ignoreCase = true),
        path.endsWith(EXT_WEBM, ignoreCase = true),
        path.endsWith(EXT_3GP, ignoreCase = true),
        path.endsWith(EXT_AVI, ignoreCase = true) -> ifNotCached { NodeContent.Movie(path) }
        path.endsWith(EXT_MP3, ignoreCase = true),
        path.endsWith(EXT_M4A, ignoreCase = true),
        path.endsWith(EXT_OGG, ignoreCase = true),
        path.endsWith(EXT_WAV, ignoreCase = true),
        path.endsWith(EXT_FLAC, ignoreCase = true),
        path.endsWith(EXT_OGA, ignoreCase = true),
        path.endsWith(EXT_AAC, ignoreCase = true) -> ifNotCached { NodeContent.Music() }
        path.endsWith(EXT_PDF, ignoreCase = true) -> ifNotCached { NodeContent.Pdf }
        path.endsWith(EXT_TORRENT, ignoreCase = true) -> ifNotCached { NodeContent.Torrent }
        path.endsWith(EXT_FAP, ignoreCase = true) -> ifNotCached { NodeContent.Fap }
        path.endsWith(EXT_EXE, ignoreCase = true) -> ifNotCached { NodeContent.ExeMs }
        path.endsWith(EXT_SWF, ignoreCase = true) -> ifNotCached { NodeContent.Flash }
        path.endsWith(EXT_PEM, ignoreCase = true),
        path.endsWith(EXT_P12, ignoreCase = true),
        path.endsWith(EXT_CRT, ignoreCase = true) -> ifNotCached { NodeContent.Cert }
        path.endsWith(EXT_OSZ, ignoreCase = true) -> ifNotCached { NodeContent.Osu.Map() }
        path.endsWith(EXT_OSK, ignoreCase = true) -> ifNotCached { NodeContent.Osu.Skin() }
        path.endsWith(EXT_OLZ, ignoreCase = true) -> ifNotCached { NodeContent.Osu.LazerMap() }
        path.endsWith(EXT_OSR, ignoreCase = true) -> ifNotCached { NodeContent.Osu.Replay() }
        path.endsWith(EXT_OSB, ignoreCase = true) -> ifNotCached { NodeContent.Osu.Storyboard() }
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
}
