package app.atomofiron.searchboxapp.utils

import android.content.pm.PackageManager
import app.atomofiron.common.util.MutableList
import app.atomofiron.common.util.property.MutableWeakProperty
import app.atomofiron.searchboxapp.logE
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeContent.Directory.Type
import app.atomofiron.searchboxapp.model.explorer.NodeContent.File.AndroidApp
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeProperties
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.explorer.NodeState
import app.atomofiron.searchboxapp.model.explorer.Operation
import app.atomofiron.searchboxapp.model.explorer.other.forNode
import app.atomofiron.searchboxapp.utils.Const.LF
import app.atomofiron.searchboxapp.utils.Const.SLASH
import kotlinx.coroutines.Job
import kotlin.math.roundToInt

object ExplorerUtils {
    // зато насколько всё становится проще
    val packageManager = MutableWeakProperty<PackageManager>()

    private const val ROOT_PARENT_PATH = "root_parent_path"

    private const val TOTAL = "total"
    private const val ROOT = SLASH.toString()
    private const val DIR_CHAR = 'd'
    private const val LINK_CHAR = 'l'
    private const val FILE_CHAR = '-'
    private const val LS_NO_SUCH_FILE = "ls: %s: No such file or directory"
    private const val LS_PERMISSION_DENIED = "ls: %s: Permission denied"
    private const val COMMAND_PATH_PREFIX = "[a-z]+: %s: "

    private const val DIRECTORY = "directory"
    private const val FILE_PNG = "PNG image data"
    private const val FILE_JPEG = "JPEG image data"
    private const val FILE_GIF = "GIF image data"
    private const val FILE_ZIP = "Zip archive data"
    private const val FILE_GZIP = "gzip compressed data"
    private const val FILE_XZ = "xz compressed data"
    private const val FILE_BZIP2 = "bzip2 compressed data"
    private const val FILE_TAR = "POSIX tar archive" // (GNU)
    private const val FILE_UTF8_TEXT = "UTF-8 text"
    private const val FILE_ASCII_TEXT = "ASCII text"
    private const val FILE_PDF = "PDF document" //, version 1.6
    private const val FILE_DATA = "data" // pdf mp4 mp3 ogg rar webp
    private const val FILE_EMPTY = "empty"
    private const val FILE_BOOTING = "Android bootimg" // img
    private const val FILE_BOOT_IMAGE = "Android boot image v2" // img
    private const val FILE_SH_SCRIPT = "/bin/sh script" // sh
    private const val FILE_OGG = "Ogg data, opus audio" // oga
    private const val FILE_PEM = "PEM certificate" // pem
    private const val FILE_ELF_EXE = "ELF executable"
    private const val FILE_ELF_RE = "ELF relocatable"
    private const val FILE_ELF_SO = "ELF shared object" //, 64-bit LSB x86-64, dynamic (/lib64/ld-linux-x86-64.so.2), not stripped
    private const val FILE_MSP_EXE = "MS PE32+ executable" // (console) x86-64, (GUI) x86-64
    private const val FILE_MS_EXE = "MS PE32 executable" //  (GUI) x86
    private const val FILE_APL_EXE = "Mach-O 64-bit x86-64 executable"
    private const val FILE_APLS_EXE = "Mach-O 64-bit arm64 executable"

    private const val EXT_PNG = ".png"
    private const val EXT_JPG = ".jpg"
    private const val EXT_JPEG = ".jpeg"
    private const val EXT_GIF = ".gif"
    private const val EXT_WEBP = ".webp"
    private const val EXT_SVG = ".svg"
    private const val EXT_APK = ".apk"
    private const val EXT_ZIP = ".zip"
    private const val EXT_APKS = ".apks"
    private const val EXT_TAR = ".tar"
    private const val EXT_BZ2 = ".bz2"
    private const val EXT_DMG = ".dmg"
    private const val EXT_GZ = ".gz"
    private const val EXT_RAR = ".rar"
    private const val EXT_TXT = ".txt"
    private const val EXT_INI = ".ini"
    private const val EXT_INO = ".ino"
    private const val EXT_KT = ".kt"
    private const val EXT_KTS = ".kts"
    private const val EXT_SWIFT = ".swift"
    private const val EXT_YAML = ".yaml"
    private const val EXT_HTML = ".html"
    private const val EXT_SH = ".sh"
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

    private val spaces = Regex(" +")
    private val slashes = Regex("/+")
    private val lastPart = Regex("(?<=/)/*[^/]+/*$|^/+\$")
    private val endingSlashes = Regex("/*$")
    private val fileType = Regex(": +")

    fun String.completePath(directory: Boolean): String {
        return when {
            this == ROOT -> ROOT
            directory -> replace(endingSlashes, SLASH.toString())
            else -> replace(endingSlashes, "")
        }
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

    fun copy(from: Node, to: Node, useSu: Boolean): Node {
        val output = Shell.exec(Shell[Shell.COPY].format(from.path, to.path), useSu)
        val new = to.updateProperties(CacheConfig(useSu))
        return when {
            output.success -> new
            else -> from.copy(error = output.error.toNodeError(from.path))
        }
    }

    fun create(parent: Node, name: String, directory: Boolean, useSu: Boolean): Node {
        var targetPath = parent.path + name
        if (directory) {
            targetPath = targetPath.completePath(directory = true)
        }
        val output = when {
            directory -> Shell.exec(Shell[Shell.MKDIR].format(targetPath), useSu)
            else -> Shell.exec(Shell[Shell.TOUCH].format(targetPath), useSu)
        }
        val content = when {
            directory -> NodeContent.Directory()
            else -> NodeContent.File.Unknown
        }
        val item = Node(path = targetPath, parentPath = parent.path, rootId = parent.rootId, content = content)
        return when {
            output.success -> item.update(CacheConfig(useSu))
            else -> item.copy(error = output.error.toNodeError(targetPath))
        }
    }

    fun Node.Companion.asRoot(path: String, type: NodeRoot.NodeRootType): Node {
        return Node(
            path = path,
            parentPath = ROOT_PARENT_PATH,
            properties = NodeProperties(name = path.name()),
            content = NodeContent.Directory(rootType = type),
        )
    }

    private fun parse(line: String, name: String? = null, size: String = ""): NodeProperties {
        val parts = line.split(spaces, 7)
        val last = parts.last()
        val time = last.substring(0, 5)
        val isFile = parts[0].firstOrNull() == FILE_CHAR
        val length = if (!isFile) 0 else parts[4].toLong()
        val hrSize = if (!isFile) size else length.toSize()
        // the name can start with spaces
        val nodeName = name ?: last.substring(6, last.length)
        // todo links name.contains('->')
        return NodeProperties(
            access = parts[0],
            owner = parts[2],
            group = parts[3],
            size = hrSize,
            date = parts[5],
            time = time,
            name = nodeName,
            length = length,
        )
    }

    private fun Long.toSize(): String {
        if (this == 0L) {
            return "0B"
        }
        var dim = 0
        var tmp = this
        var secondary = 0L
        while (tmp >= 1024 && dim < 5) {
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
            builder.append('.')
            builder.append((secondary / 100f).roundToInt())
        }
        builder.append("BKMGT"[dim])
        return builder.toString()
    }

    private fun parse(parentPath: String, root: Int, properties: NodeProperties): Node {
        val incompletePath = parentPath + properties.name
        val content = when (properties.access[0]) {
            DIR_CHAR -> NodeContent.Directory(Type.Ordinary)
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

    fun getDirectoryType(name: String): Type {
        return when (name) {
            "Android" -> Type.Android
            "DCIM" -> Type.Camera
            "Download" -> Type.Download
            "Movies" -> Type.Movies
            "Music" -> Type.Music
            "Pictures" -> Type.Pictures
            else -> Type.Ordinary
        }
    }

    private fun Node.updateProperties(config: CacheConfig): Node {
        val output = Shell.exec(Shell[Shell.LS_LALD_FILE].format(path, path), config.useSu)
        val lines = output.output.trim().split(LF)
        return when {
            output.success && lines.size == 2 -> parseNode(lines.first())
                .resolveType(type = lines.last(), path = path)
            output.success -> copy(children = null, error = NodeError.Unknown)
            else -> copy(error = output.error.toNodeError(path))
        }
    }

    fun Node.update(config: CacheConfig): Node {
        val output = Shell.exec(Shell[Shell.LS_LALD_FILE].format(path, path), config.useSu)
        val lines = output.output.trim().split(LF)
        return when {
            output.success && lines.size == 2 -> parseNode(lines.first())
                .resolveType(type = lines.last(), path = path)
                .ensureCached(config, oldProps = properties)
            output.success -> copy(children = null, error = null)
            else -> copy(error = output.error.toNodeError(path))
        }
    }

    private fun Node.ensureCached(config: CacheConfig, oldProps: NodeProperties): Node = when {
        isDirectory -> cacheDir(config.useSu)
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

    private fun Node.cacheDir(useSu: Boolean): Node {
        val output = Shell.exec(Shell[Shell.LS_LAL].format(path), useSu)
        val lines = output.output.split(LF).filter { it.isNotEmpty() }
        return when {
            output.success && lines.isEmpty() -> copy(children = null, error = null)
            output.success -> parseDir(lines)
            else -> copy(error = output.error.toNodeError(path))
        }
    }

    /** resolve content types */
    fun Node.resolveDirChildren(useSu: Boolean): Boolean {
        val children = children ?: return false
        val output = Shell.exec(Shell[Shell.CD_FILE_CHILDREN].format(path), useSu)
        val lines = output.output.split(LF).filter { it.isNotEmpty() }
        when {
            !output.success -> return false
            lines.isEmpty() -> return false
        }
        lines.map { it.split(fileType) }.forEach { (path, type) ->
            val name = path.substring(2) // remove Shell.DOT_SLASH = "./"
            val index = children.items
                .indexOfFirst { it.name == name }
                .also { if (it < 0) return@forEach }
            children.run {
                items[index] = items[index].resolveType(type = type)
            }
        }
        return true
    }

    fun Node.resolveSize(useSu: Boolean): String = Shell.exec(Shell[Shell.DU_HD0].format(path), useSu)
            .output
            .trim()
            .split(Const.TAB)
            .takeIf { it.size == 2 }
            ?.firstOrNull()
            ?: ""

    private fun Node.resolveType(type: String, path: String): Node {
        return resolveType(type.substring(path.length.inc()).trim())
    }

    private fun Node.resolveType(type: String): Node {
        val content = when (true) {
            (content is NodeContent.Directory) -> content
            type.isBlank(),
            (type == FILE_DATA) -> content.resolveFileType(path)
            (type == FILE_EMPTY) -> content.resolveFileType(path)
            (type == DIRECTORY) -> content.ifNotCached { NodeContent.Directory() }
            type.startsWith(FILE_PNG) -> content.ifNotCached { NodeContent.File.Picture.Png(path) }
            type.startsWith(FILE_JPEG) -> content.ifNotCached { NodeContent.File.Picture.Jpeg(path) }
            type.startsWith(FILE_GIF) -> content.ifNotCached { NodeContent.File.Picture.Gif(path) }
            type.startsWith(FILE_ZIP) -> when {
                path.endsWith(EXT_APK, ignoreCase = true) -> content.ifNotCached { AndroidApp.Apk(path) }
                path.endsWith(EXT_APKS, ignoreCase = true) -> content.ifNotCached { AndroidApp.Apks(path) }
                content is AndroidApp -> return this
                path.endsWith(EXT_OSZ, ignoreCase = true) -> content.ifNotCached { NodeContent.File.Osu.Map() }
                path.endsWith(EXT_OSK, ignoreCase = true) -> content.ifNotCached { NodeContent.File.Osu.Skin() }
                path.endsWith(EXT_OLZ, ignoreCase = true) -> content.ifNotCached { NodeContent.File.Osu.LazerMap() }
                path.endsWith(EXT_OSR, ignoreCase = true) -> content.ifNotCached { NodeContent.File.Osu.Replay() }
                path.endsWith(EXT_OSB, ignoreCase = true) -> content.ifNotCached { NodeContent.File.Osu.Storyboard() }
                else -> content.ifNotCached { NodeContent.File.Zip() }
            }
            type.startsWith(FILE_BZIP2) -> when {
                name.endsWith(EXT_DMG) -> content.ifNotCached { NodeContent.File.Dmg }
                else -> content.ifNotCached { NodeContent.File.Bzip2() }
            }
            type.startsWith(FILE_GZIP) -> content.ifNotCached { NodeContent.File.Gz() }
            type.startsWith(FILE_TAR) -> content.ifNotCached { NodeContent.File.Tar() }
            type.startsWith(FILE_XZ) -> content.ifNotCached { NodeContent.File.Xz }
            type.startsWith(FILE_SH_SCRIPT) -> NodeContent.File.Text.Script
            type.startsWith(FILE_UTF8_TEXT),
            type.startsWith(FILE_ASCII_TEXT) -> when {
                path.endsWith(EXT_SVG, ignoreCase = true) -> content.ifNotCached { NodeContent.File.Text.Svg }
                path.endsWith(EXT_OSU, ignoreCase = true) -> content.ifNotCached { NodeContent.File.Text.Osu }
                path.endsWith(EXT_INO, ignoreCase = true) -> content.ifNotCached { NodeContent.File.Text.Ino }
                else -> NodeContent.File.Text.Plain
            }
            type.startsWith(FILE_BOOTING),
            type.startsWith(FILE_BOOT_IMAGE) -> NodeContent.File.DataImage
            type.startsWith(FILE_OGG) -> content.ifNotCached { NodeContent.File.Music() }
            type.startsWith(FILE_PDF) -> content.ifNotCached { NodeContent.File.Pdf }
            type.startsWith(FILE_ELF_EXE) -> content.ifNotCached { NodeContent.File.Elf }
            type.startsWith(FILE_ELF_RE) -> when {
                name.endsWith(EXT_FAP) -> content.ifNotCached { NodeContent.File.Fap }
                else -> content.ifNotCached { NodeContent.File.Elf }
            }
            type.startsWith(FILE_PEM) -> content.ifNotCached { NodeContent.File.Cert }
            type.startsWith(FILE_ELF_SO) -> content.ifNotCached { NodeContent.File.ElfSo }
            type.startsWith(FILE_MSP_EXE),
            type.startsWith(FILE_MS_EXE) -> content.ifNotCached { NodeContent.File.ExeMs }
            type.startsWith(FILE_APLS_EXE) -> content.ifNotCached { NodeContent.File.ExeApls }
            type.startsWith(FILE_APL_EXE) -> content.ifNotCached { NodeContent.File.ExeApl }
            else -> {
                val ext = name.lastIndexOf(Const.DOT).inc()
                    .let { if (it == 0) name.length else it }
                    .let { name.substring(it) }
                logE("'$ext' unknown type: $type")
                content.resolveFileType(path)
            }
        }
        return copy(content = content)
    }

    private fun Node.cacheFile(config: CacheConfig): Node {
        val content = when (content) {
            is NodeContent.File.Picture.Png -> NodeContent.File.Picture.Png(path)
            is NodeContent.File.Picture.Jpeg -> NodeContent.File.Picture.Jpeg(path)
            is NodeContent.File.Picture.Gif -> NodeContent.File.Picture.Gif(path)
            is NodeContent.File.Picture.Webp -> NodeContent.File.Picture.Webp(path)
            is NodeContent.File.Picture.Avif -> NodeContent.File.Picture.Avif(path)
            is NodeContent.File.Movie -> NodeContent.File.Movie(path)
            is NodeContent.File.Music -> NodeContent.File.Music(0, path.createAudioThumbnail(config)?.forNode)
            is NodeContent.File.Zip -> AndroidApp.Apks(path)
                .tryGetApksContent(path)
                .contentOrNodeError(this) { return it }
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

    private inline fun Rslt<out NodeContent>.contentOrNodeError(node: Node, action: (withError: Node) -> Nothing): NodeContent {
        return unwrapOrElse {
            action(node.copy(error = NodeError.Message(it)))
        }
    }

    private fun AndroidApp.tryGetApksContent(zipPath: String): Rslt<AndroidApp> = try {
        getApksContent(zipPath)
    } catch (e: Exception) {
        Rslt.Err(e.toString())
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

    private fun Node.parseNode(line: String): Node {
        val properties = parse(line, name, size)
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
            else -> null to NodeContent.Unknown
        }
        return copy(children = children, properties = properties, content = content)
    }

    private fun Node.parseDir(lines: List<String>): Node {
        val items = MutableList<Node>(lines.size)
        val files = MutableList<Node>(lines.size)
        val start = if (lines.first().startsWith(TOTAL)) 1 else 0
        for (i in start until lines.size) {
            val line = lines[i]
            if (line.isNotEmpty()) {
                var properties = parse(line)
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
                    item.content is NodeContent.Unknown -> files.add(item.copy(content = resolveFileType(item.path)))
                    else -> files.add(item)
                }
            }
        }
        items.addAll(files)
        val directoryType = when (content) {
            is NodeContent.Directory -> content.type
            else -> Type.Ordinary
        }
        return copy(
            children = NodeChildren(items, isOpened = children?.isOpened == true),
            content = NodeContent.Directory(directoryType, content.rootType),
            error = null,
        )
    }

    fun Node.open(value: Boolean = true): Node = when {
        children == null -> this
        children.isOpened == value -> this
        else -> {
            if (!value) children.clearChildren()
            copy(children = children.copy(isOpened = value))
        }
    }

    fun Node.close(): Node = open(false)

    fun Node.isParentOf(other: Node): Boolean = other.parentPath == path

    fun Node.isSomeParentOf(other: Node): Boolean = path.length <= other.path.length && other.path.startsWith(path)

    private fun NodeChildren.clearChildren() = update {
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
        this!!
        other!!.forEachIndexed { i, it ->
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
    fun Node.isDot(): Boolean = path.endsWith("/.")

    fun Node.withoutDot(): String = when {
        isDot() -> path.substring(0, path.length.dec())
        else -> path
    }

    fun Node.delete(useSu: Boolean): Node? {
        val output = Shell.exec(Shell[Shell.RM_RF].format(path), useSu)
        return when {
            output.success -> null
            else -> copy(error = output.error.toNodeError(path))
        }
    }

    fun Node.rename(name: String, useSu: Boolean): Node {
        val targetPath = parentPath + name
        val output = Shell.exec(Shell[Shell.MV].format(path, targetPath), useSu)
        return when {
            output.success -> rename(name).copy(error = null)
            else -> copy(error = output.error.toNodeError(path))
        }
    }

    private fun String.toNodeError(path: String): NodeError {
        val lines = trim().split(LF)
        val first = lines.find { it.isNotBlank() }
        return when {
            lines.size > 1 -> NodeError.Multiply(lines)
            first.isNullOrBlank() -> NodeError.Unknown
            path.isBlank() -> NodeError.Message(first)
            first == LS_NO_SUCH_FILE.format(path) -> NodeError.NoSuchFile
            first == LS_PERMISSION_DENIED.format(path) -> NodeError.PermissionDenied
            else -> NodeError.Message(first.replace(Regex(COMMAND_PATH_PREFIX.format(path)), ""))
        }
    }

    fun NodeState?.theSame(cachingJob: Job?, operation: Operation): Boolean {
        val currentOperation = this?.operation ?: Operation.None
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
        path.endsWith(EXT_PNG, ignoreCase = true) -> ifNotCached { NodeContent.File.Picture.Png(path) }
        path.endsWith(EXT_JPG, ignoreCase = true),
        path.endsWith(EXT_JPEG, ignoreCase = true) -> ifNotCached { NodeContent.File.Picture.Jpeg(path) }
        path.endsWith(EXT_GIF, ignoreCase = true) -> ifNotCached { NodeContent.File.Picture.Gif(path) }
        path.endsWith(EXT_WEBP, ignoreCase = true) -> ifNotCached { NodeContent.File.Picture.Webp(path) }
        path.endsWith(EXT_AVIF, ignoreCase = true) -> ifNotCached { NodeContent.File.Picture.Avif(path) }
        path.endsWith(EXT_APK, ignoreCase = true) -> ifNotCached { AndroidApp.Apk(path) }
        path.endsWith(EXT_APKS, ignoreCase = true) -> ifNotCached { AndroidApp.Apks(path) }
        path.endsWith(EXT_ZIP, ignoreCase = true) -> ifNotCached { NodeContent.File.Zip() }
        path.endsWith(EXT_TAR, ignoreCase = true) -> ifNotCached { NodeContent.File.Tar() }
        path.endsWith(EXT_BZ2, ignoreCase = true) -> ifNotCached { NodeContent.File.Bzip2() }
        path.endsWith(EXT_GZ, ignoreCase = true) -> ifNotCached { NodeContent.File.Gz() }
        path.endsWith(EXT_RAR, ignoreCase = true) -> ifNotCached { NodeContent.File.Rar() }
        path.endsWith(EXT_SH, ignoreCase = true) -> NodeContent.File.Text.Script
        path.endsWith(EXT_TXT, ignoreCase = true),
        path.endsWith(EXT_INI, ignoreCase = true),
        path.endsWith(EXT_KT, ignoreCase = true),
        path.endsWith(EXT_KTS, ignoreCase = true),
        path.endsWith(EXT_SWIFT, ignoreCase = true),
        path.endsWith(EXT_YAML, ignoreCase = true),
        path.endsWith(EXT_HTML, ignoreCase = true) -> NodeContent.File.Text.Plain
        path.endsWith(EXT_SVG, ignoreCase = true) -> ifNotCached { NodeContent.File.Text.Svg }
        path.endsWith(EXT_IMG, ignoreCase = true) -> NodeContent.File.DataImage
        path.endsWith(EXT_MP4, ignoreCase = true),
        path.endsWith(EXT_MKV, ignoreCase = true),
        path.endsWith(EXT_MOV, ignoreCase = true),
        path.endsWith(EXT_WEBM, ignoreCase = true),
        path.endsWith(EXT_3GP, ignoreCase = true),
        path.endsWith(EXT_AVI, ignoreCase = true) -> ifNotCached { NodeContent.File.Movie(path) }
        path.endsWith(EXT_MP3, ignoreCase = true),
        path.endsWith(EXT_M4A, ignoreCase = true),
        path.endsWith(EXT_OGG, ignoreCase = true),
        path.endsWith(EXT_WAV, ignoreCase = true),
        path.endsWith(EXT_FLAC, ignoreCase = true),
        path.endsWith(EXT_OGA, ignoreCase = true),
        path.endsWith(EXT_AAC, ignoreCase = true) -> ifNotCached { NodeContent.File.Music() }
        path.endsWith(EXT_PDF, ignoreCase = true) -> ifNotCached { NodeContent.File.Pdf }
        path.endsWith(EXT_TORRENT, ignoreCase = true) -> ifNotCached { NodeContent.File.Torrent }
        path.endsWith(EXT_FAP, ignoreCase = true) -> ifNotCached { NodeContent.File.Fap }
        path.endsWith(EXT_EXE, ignoreCase = true) -> ifNotCached { NodeContent.File.ExeMs }
        path.endsWith(EXT_SWF, ignoreCase = true) -> ifNotCached { NodeContent.File.Flash }
        path.endsWith(EXT_PEM, ignoreCase = true),
        path.endsWith(EXT_P12, ignoreCase = true),
        path.endsWith(EXT_CRT, ignoreCase = true) -> ifNotCached { NodeContent.File.Cert }
        else -> NodeContent.File.Other
    }

    fun Node.updateWith(item: Node, sorting: NodeSorting? = null): Node {
        var children = when {
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
        if (children != null && children.isOpened != isOpened) {
            children = children.copy(isOpened = isOpened)
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
