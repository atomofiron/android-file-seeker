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
import app.atomofiron.searchboxapp.model.explorer.NodeRootType
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.explorer.NodeState
import app.atomofiron.searchboxapp.model.explorer.other.forNode
import app.atomofiron.searchboxapp.utils.Const.LF
import app.atomofiron.searchboxapp.utils.Const.SLASH
import kotlinx.coroutines.Job
import uniffi.native_lib.*
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
    private const val FILE_PICTURE = "image/"
    private const val FILE_AUDIO = "audio/"
    private const val FILE_VIDEO = "video/"
    private const val FILE_TEXT_SCRIPT = "text/x-"
    private const val FILE_TEXT = "text/"
    private const val FILE_UNKNOWN = "application/octet-stream"
    private const val FILE_XML = "application/xml"
    private const val FILE_ZIP = "application/zip"
    private const val FILE_APK = "application/vnd.android.package-archive"
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
    private const val EXT_APK = ".apk"
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
            is Rslt.Ok -> output.value
            is Rslt.Err -> return null
        }
        return Node(path = targetPath, parentPath = parent.path, rootId = parent.rootId, properties = meta.toProperties(), content = content)
    }

    fun Node.Companion.asRoot(path: String, type: NodeRootType): Node {
        return Node(
            path = path,
            parentPath = ROOT_PARENT_PATH,
            properties = NodeProperties(name = path.name()),
            content = NodeContent.Directory(rootType = type),
        )
    }

    private fun Meta.toProperties(
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
            length = if (!isFile) 0 else length.toLong(),
            size = this.size.takeIf { it.isNotEmpty() } ?: size,
        )
    }

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

    private fun parse(parentPath: String, root: Int, properties: NodeProperties): Node {
        val incompletePath = parentPath + properties.name
        val content = when (properties.access.firstOrNull()) {
            DIR_CHAR -> NodeContent.Directory(DirectoryKind.Ordinary)
            LINK_CHAR -> NodeContent.Link
            null -> NodeContent.Unknown
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
        val type = NativeBridge.type(path, config.asSu)
        return when (type) {
            is Rslt.Ok -> parseNode(type.value.meta).resolveType(type.value.mime)
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
            is Rslt.Ok -> parseDir(result.value)
            is Rslt.Err -> copy(error = result.message.toNodeError(path))
        }
    }

    /** resolve content types */
    fun Node.resolveDirChildren(asSu: Boolean): Boolean {
        val children = children ?: return false
        val types = NativeBridge.types(path, asSu)
        val entries = when (types) {
            is Rslt.Ok -> types.value
            is Rslt.Err -> return false
        }
        entries.forEach { entry ->
            val index = children.items
                .indexOfFirst { it.name == entry.meta.name }
                .also { if (it < 0) return@forEach }
            children.run {
                val child = items[index]
                items[index] = child.resolveType(mimeType = entry.mime)
                    .copy(properties = entry.meta.toProperties(child.name, child.size))
            }
        }
        return entries.isNotEmpty()
    }

    private fun Node.resolveType(mimeType: String): Node {
        val content = when (true) {
            (access.firstOrNull() == DIR_CHAR),
            (mimeType == DIRECTORY),
            (content is NodeContent.Directory) -> content.ifNotCached { NodeContent.Directory() }
            (length == 0L) -> NodeContent.Empty
            mimeType.isBlank(),
            (mimeType == FILE_UNKNOWN) -> content.resolveFileType(path)
            mimeType.startsWith(FILE_PICTURE) -> content.ifNotCached { NodeContent.Picture.resolve(mimeType) }
            (mimeType == FILE_XRIFF) -> content.ifNotCached { NodeContent.Picture(mimeType) }
            (mimeType == FILE_APK) -> content.ifNotCached { AndroidApp.apk(path) }
            (mimeType == FILE_ZIP) -> when (true) {
                path.hasExt(EXT_APKS),
                path.hasExt(EXT_APKM) -> content.ifNotCached { AndroidApp.apks(path) }
                (content is AndroidApp) -> return this
                path.hasExt(EXT_OSZ) -> content.ifNotCached { NodeContent.Osu.Map() }
                path.hasExt(EXT_OSK) -> content.ifNotCached { NodeContent.Osu.Skin() }
                path.hasExt(EXT_OLZ) -> content.ifNotCached { NodeContent.Osu.LazerMap() }
                path.hasExt(EXT_OSR) -> content.ifNotCached { NodeContent.Osu.Replay() }
                path.hasExt(EXT_OSB) -> content.ifNotCached { NodeContent.Osu.Storyboard() }
                else -> content.ifNotCached { NodeContent.Zip() }
            }
            (mimeType == FILE_BZIP2) -> when {
                name.hasExt(EXT_DMG) -> content.ifNotCached { NodeContent.Dmg }
                else -> content.ifNotCached { NodeContent.Bzip2() }
            }
            (mimeType == FILE_GZIP) -> content.ifNotCached { NodeContent.Gz() }
            (mimeType == FILE_TAR) -> content.ifNotCached { NodeContent.Tar() }
            (mimeType == FILE_XZ) -> content.ifNotCached { NodeContent.Xz }
            mimeType.startsWith(FILE_TEXT) -> when {
                path.hasExt(EXT_SVG) -> content.ifNotCached { NodeContent.Text.Svg }
                path.hasExt(EXT_OSU) -> content.ifNotCached { NodeContent.Text.Osu }
                path.hasExt(EXT_CPP) -> content.ifNotCached { NodeContent.Text.Cpp }
                path.hasExt(EXT_INO) -> content.ifNotCached { NodeContent.Text.Ino }
                path.hasExt(EXT_BAT) -> content.ifNotCached { NodeContent.Text.BatScript }
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
                logE("'${name.getExt()}' unknown type: $mimeType ${path.takeIfDebug()}")
                content.resolveFileType(path)
            }
        }
        return copy(content = content)
    }

    private fun Node.cacheFile(config: CacheConfig): Node {
        val content = when (content) {
            is NodeContent.Picture,
            is NodeContent.Movie -> content
            is NodeContent.Music -> content.copy(thumbnail = path.createAudioThumbnail(config)?.forNode)
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

    private fun Node.parseNode(meta: Meta): Node {
        val properties = meta.toProperties(name, size)
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

    private fun Node.parseDir(metas: List<Meta>): Node {
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
        !isSeparator() -> throw UnsupportedOperationException() // todo
        else -> path.substring(0, path.length.dec())
    }

    fun Node.withoutDot(): String = when {
        isSeparator() -> path.substring(0, path.length.dec())
        else -> path
    }

    fun Node.delete(asSu: Boolean): Node? {
        val result = NativeBridge.delete(path, asSu)
        val error = when (result) {
            is Rslt.Ok -> return null
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
        path.hasExt(EXT_APNG) -> ifNotCached { NodeContent.Picture.Apng }
        path.hasExt(EXT_PNG) -> ifNotCached { NodeContent.Picture.Png }
        path.hasExt(EXT_JPG),
        path.hasExt(EXT_JPEG) -> ifNotCached { NodeContent.Picture.Jpeg }
        path.hasExt(EXT_GIF) -> ifNotCached { NodeContent.Picture.Gif }
        path.hasExt(EXT_WEBP) -> ifNotCached { NodeContent.Picture.Webp }
        path.hasExt(EXT_AVIF) -> ifNotCached { NodeContent.Picture.Avif }
        path.hasExt(EXT_APK) -> ifNotCached { AndroidApp.apk(path) }
        path.hasExt(EXT_DEX),
        path.hasExt(EXT_ODEX),
        path.hasExt(EXT_VDEX) -> ifNotCached { NodeContent.Java }
        path.hasExt(EXT_APKS),
        path.hasExt(EXT_APKM) -> ifNotCached { AndroidApp.apks(path) }
        path.hasExt(EXT_ZIP),
        path.hasExt(EXT_XAPK) -> ifNotCached { NodeContent.Zip() }
        path.hasExt(EXT_TAR) -> ifNotCached { NodeContent.Tar() }
        path.hasExt(EXT_BZ2) -> ifNotCached { NodeContent.Bzip2() }
        path.hasExt(EXT_GZ) -> ifNotCached { NodeContent.Gz() }
        path.hasExt(EXT_RAR) -> ifNotCached { NodeContent.Rar() }
        path.hasExt(EXT_SH) -> NodeContent.Text.ShellScript
        path.hasExt(EXT_BAT) -> NodeContent.Text.BatScript
        path.hasExt(EXT_TXT),
        path.hasExt(EXT_INI),
        path.hasExt(EXT_JAVA),
        path.hasExt(EXT_KT),
        path.hasExt(EXT_KTS),
        path.hasExt(EXT_SWIFT),
        path.hasExt(EXT_YAML),
        path.hasExt(EXT_HTML) -> NodeContent.Text.Plain
        path.hasExt(EXT_SVG) -> ifNotCached { NodeContent.Text.Svg }
        path.hasExt(EXT_IMG) -> NodeContent.DataImage
        path.hasExt(EXT_MP4) -> ifNotCached { NodeContent.Movie.Mp4 }
        path.hasExt(EXT_MKV) -> ifNotCached { NodeContent.Movie.Mkv }
        path.hasExt(EXT_MOV) -> ifNotCached { NodeContent.Movie.Mov }
        path.hasExt(EXT_WEBM) -> ifNotCached { NodeContent.Movie.Webm }
        path.hasExt(EXT_3GP) -> ifNotCached { NodeContent.Movie.Tgp }
        path.hasExt(EXT_AVI) -> ifNotCached { NodeContent.Movie.Avi }
        path.hasExt(EXT_MP3) -> ifNotCached { NodeContent.Music.Mp3 }
        path.hasExt(EXT_M4A) -> ifNotCached { NodeContent.Music.M4a }
        path.hasExt(EXT_OGA),
        path.hasExt(EXT_OGG) -> ifNotCached { NodeContent.Music.Ogg }
        path.hasExt(EXT_WAV) -> ifNotCached { NodeContent.Music.Wav }
        path.hasExt(EXT_FLAC) -> ifNotCached { NodeContent.Music.Flac }
        path.hasExt(EXT_AAC) -> ifNotCached { NodeContent.Music.Aac }
        path.hasExt(EXT_PDF) -> ifNotCached { NodeContent.Pdf }
        path.hasExt(EXT_TORRENT) -> ifNotCached { NodeContent.Torrent }
        path.hasExt(EXT_FAP) -> ifNotCached { NodeContent.Fap }
        path.hasExt(EXT_EXE) -> ifNotCached { NodeContent.ExeMs }
        path.hasExt(EXT_SWF) -> ifNotCached { NodeContent.Flash }
        path.hasExt(EXT_PEM),
        path.hasExt(EXT_P12),
        path.hasExt(EXT_CRT) -> ifNotCached { NodeContent.Cert }
        path.hasExt(EXT_OSZ) -> ifNotCached { NodeContent.Osu.Map() }
        path.hasExt(EXT_OSK) -> ifNotCached { NodeContent.Osu.Skin() }
        path.hasExt(EXT_OLZ) -> ifNotCached { NodeContent.Osu.LazerMap() }
        path.hasExt(EXT_OSR) -> ifNotCached { NodeContent.Osu.Replay() }
        path.hasExt(EXT_OSB) -> ifNotCached { NodeContent.Osu.Storyboard() }
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
