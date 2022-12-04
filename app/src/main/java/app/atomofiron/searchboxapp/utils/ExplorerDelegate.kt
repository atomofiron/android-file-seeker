package app.atomofiron.searchboxapp.utils

import android.content.Context
import android.util.Log
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.*
import app.atomofiron.searchboxapp.model.explorer.NodeContent.Directory.Type
import app.atomofiron.searchboxapp.utils.MediaDelegate.getThumbnail
import kotlinx.coroutines.Job

object ExplorerDelegate {
    const val ROOT_PARENT_PATH = "root_parent_path"

    private const val TOTAL = "total"
    private const val SLASH = "/"
    private const val NEW_LINE = "\n"
    private const val ROOT = SLASH
    private const val ROOT_NAME = "Root"
    private const val DIR_CHAR = 'd'
    private const val LINK_CHAR = 'l'
    private const val FILE_CHAR = '-'
    private const val LS_NO_SUCH_FILE = "ls: %s: No such file or directory"
    private const val LS_PERMISSION_DENIED = "ls: %s: Permission denied"
    private const val COMMAND_PATH_PREFIX = "[a-z]+: %s: "

    private const val FILE_PNG = "PNG image data"
    private const val FILE_JPEG = "JPEG image data"
    private const val FILE_ZIP = "Zip archive data"
    private const val FILE_GZIP = "gzip compressed data"
    private const val FILE_BZIP2 = "bzip2 compressed data"
    private const val FILE_TAR = "POSIX tar archive"
    private const val FILE_UTF8_TEXT = "UTF-8 text"
    private const val FILE_ASCII_TEXT = "ASCII text"
    private const val FILE_DATA = "data" // pdf mp4 mp3 ogg rar webp
    private const val FILE_EMPTY = "empty"
    private const val FILE_BOOTING = "Android bootimg" // img
    private const val FILE_SH_SCRIPT = "/bin/sh script" // sh
    private const val FILE_OGG = "Ogg data, opus audio" // oga

    private const val EXT_PNG = ".png"
    private const val EXT_JPG = ".jpg"
    private const val EXT_JPEG = ".jpeg"
    private const val EXT_GIF = ".gif"
    private const val EXT_WEBP = ".webp"
    private const val EXT_APK = ".apk"
    private const val EXT_ZIP = ".zip"
    private const val EXT_TAR = ".tar"
    private const val EXT_BZ2 = ".bz2"
    private const val EXT_GZ = ".gz"
    private const val EXT_RAR = ".rar"
    private const val EXT_TXT = ".txt"
    private const val EXT_HTML = ".html"
    private const val EXT_SH = ".sh"
    private const val EXT_IMG = ".img"
    private const val EXT_MP4 = ".mp4"
    private const val EXT_3GP = ".3gp"
    private const val EXT_AVI = ".avi"
    private const val EXT_MP3 = ".mp3"
    private const val EXT_OGG = ".ogg"
    private const val EXT_WAV = ".wav"
    private const val EXT_FLAC = ".flac"
    private const val EXT_AAC = ".aac"
    private const val EXT_OGA = ".oga"
    private const val EXT_PDF = ".pdf"
    private const val EXT_EXE = ".exe"
    private const val EXT_XPI = ".xpi" // Mozilla extension

    private val spaces = Regex(" +")
    private val slashes = Regex("/+")
    private val lastPart = Regex("(?<=/)/*[^/]+/*$|^/+\$")
    private val endingSlashes = Regex("/*$")

    fun Context.getExternalStorageDirectory(): String? {
        val absolutePath = getExternalFilesDir(null)?.absolutePath
        absolutePath ?: return null
        val index = absolutePath.indexOf(Const.ANDROID_DIR).inc()
        return when (index) {
            0 -> null
            else -> absolutePath.substring(0, index)
        }
    }

    fun completePath(absolutePath: String, isDirectory: Boolean): String {
        return when {
            absolutePath == ROOT -> ROOT
            isDirectory -> absolutePath.replace(endingSlashes, SLASH)
            else -> absolutePath.replace(endingSlashes, "")
        }
    }

    fun String.parent(): String = replace(lastPart, "")

    fun String.name(): String = split(slashes).findLast { it.isNotEmpty() } ?: ROOT_NAME

    fun completeDirPath(absolutePath: String): String = completePath(absolutePath, isDirectory = true)

    fun create(parent: Node, name: String, isDirectory: Boolean): Node {
        val content = when {
            isDirectory -> NodeContent.Directory(Type.Ordinary)
            else -> NodeContent.File.Unknown
        }
        return Node(
            rootId = parent.rootId,
            path = parent.path + name,
            parentPath = parent.path,
            properties = NodeProperties(name = name),
            content = content,
        )
    }

    fun create(parent: Node, name: String, directory: Boolean, useSu: Boolean): Node {
        var targetPath = parent.path + name
        if (directory) {
            targetPath = completeDirPath(targetPath)
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

    fun asRoot(path: String): Node {
        return Node(
            path = path,
            parentPath = ROOT_PARENT_PATH,
            properties = NodeProperties(name = path.name()),
            content = NodeContent.Unknown,
        )
    }

    private fun parse(line: String, name: String? = null): NodeProperties {
        val parts = line.split(spaces, 7)
        val last = parts.last()
        val time = last.substring(0, 5)
        // the name can start with spaces
        val nodeName = name ?: last.substring(6, last.length)
        // todo links name.contains('->')
        return NodeProperties(
            access = parts[0],
            owner = parts[2],
            group = parts[3],
            size = parts[4],
            date = parts[5],
            time = time,
            name = nodeName,
        )
    }

    private fun parse(parentPath: String, line: String, root: Int): Node {
        val properties = parse(line)
        val content = when (properties.access[0]) {
            DIR_CHAR -> NodeContent.Directory(Type.Ordinary)
            LINK_CHAR -> NodeContent.Link
            else -> properties.name.resolveFileType()
        }
        val asDir = content is NodeContent.Directory
        return Node(
            rootId = root,
            path = completePath(parentPath + properties.name, asDir),
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

    fun Node.update(config: CacheConfig): Node {
        val output = Shell.exec(Shell[Shell.LS_LAHLD].format(path), config.useSu)
        val lines = output.output.split("\n").filter { it.isNotEmpty() }
        return when {
            output.success && lines.size == 1 -> parseNode(lines.first()).run {
                when {
                    isDirectory -> cacheDir(config.useSu)
                    isCached -> this
                    else -> cacheFile(config)
                }
            }
            output.success -> copy(children = null, error = NodeError.Unknown)
            else -> copy(error = output.error.toNodeError(path))
        }
    }

    private fun Node.cacheDir(useSu: Boolean): Node {
        val output = Shell.exec(Shell[Shell.LS_LAHL].format(path), useSu)
        val lines = output.output.split("\n").filter { it.isNotEmpty() }
        return when {
            output.success && lines.isEmpty() -> copy(children = null, error = NodeError.Unknown)
            output.success -> parseDir(lines)
            else -> copy(error = output.error.toNodeError(path))
        }
    }

    private fun Node.cacheFile(config: CacheConfig): Node {
        val output = Shell.exec(Shell[Shell.FILE_B].format(path), config.useSu)
        var content = content
        content = when {
            !output.success || output.output.isBlank() -> path.resolveFileType()
            output.output.startsWith(FILE_PNG) -> content.ifEmpty { NodeContent.File.Picture.Png(path.getThumbnail(config)) }
            output.output.startsWith(FILE_JPEG) -> content.ifEmpty { NodeContent.File.Picture.Jpeg(path.getThumbnail(config)) }
            output.output.startsWith(FILE_ZIP) -> when {
                path.endsWith(EXT_APK, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Apk() }
                else -> content.ifEmpty { NodeContent.File.Archive.Zip() }
            }
            output.output.startsWith(FILE_BZIP2) -> content.ifEmpty { NodeContent.File.Archive.Bzip2() }
            output.output.startsWith(FILE_GZIP) -> content.ifEmpty { NodeContent.File.Archive.Gz() }
            output.output.startsWith(FILE_TAR) -> content.ifEmpty { NodeContent.File.Archive.Tar() }
            output.output.startsWith(FILE_SH_SCRIPT) -> NodeContent.File.Text.Script
            output.output.startsWith(FILE_UTF8_TEXT) ||
            output.output.startsWith(FILE_ASCII_TEXT) -> NodeContent.File.Text.Plain
            output.output.startsWith(FILE_DATA) -> path.resolveFileType(content, config)
            output.output.startsWith(FILE_EMPTY) -> NodeContent.File.Other
            output.output.startsWith(FILE_BOOTING) -> NodeContent.File.DataImage
            output.output.startsWith(FILE_OGG) -> content.ifEmpty { NodeContent.File.Music() }
            else -> {
                Log.e("searchboxapp", "$path ${output.output}")
                NodeContent.File.Other
            }
        }
        return copy(content = content)
    }

    private inline fun <reified T : NodeContent.File> NodeContent?.ifEmpty(action: () -> T): T {
        return if (this !is T || isEmpty) action() else this
    }

    fun Node.sortByName(): Node {
        children?.items?.run {
            sortBy { it.name.lowercase() }
            sortBy { if (it.isDirectory) 0 else 1 }
        }
        return this
    }

    fun Node.sortByDate(newFirst: Boolean = true): Node {
        children?.items?.run {
            sortBy { it.time }
            sortBy { it.date }
            if (newFirst) reverse()
            sortBy { if (it.isDirectory) 0 else 1 }
        }
        return this
    }

    private fun Node.parseNode(line: String): Node {
        val properties = parse(line, name)
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
                else -> null to NodeContent.File.Unknown
            }
            else -> null to NodeContent.Unknown
        }
        return copy(children = children, properties = properties, content = content)
    }

    private fun Node.parseDir(lines: List<String>): Node {
        val items = ArrayList<Node>(lines.size)
        val files = ArrayList<Node>(lines.size)
        val start = if (lines.first().startsWith(TOTAL)) 1 else 0
        for (i in start until lines.size) {
            val line = lines[i]
            if (line.isNotEmpty()) {
                val properties = parse(line)
                val child = children?.find { it.name == properties.name }
                val item = when {
                    child == null -> parse(path, line, rootId)
                    child.properties == properties -> child
                    else -> child.copy(properties = properties)
                }
                when {
                    item.isDirectory -> items.add(item)
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
            content = NodeContent.Directory(directoryType),
        )
    }

    private fun Node.open(value: Boolean): Node = when {
        children == null -> this
        children.isOpened == value -> this
        else -> {
            if (!value) children.clearChildren()
            copy(children = children.copy(isOpened = value))
        }
    }

    fun Node.open(): Node = open(true)

    fun Node.close(): Node = open(false)

    fun Node.isParentOf(other: Node): Boolean = other.parentPath == path

    fun Node.isTheDeepest(): Boolean = isOpened && children != null && children.find { it.isOpened } == null

    private fun NodeChildren.clearChildren() {
        val iter = items.listIterator()
        while (iter.hasNext()) {
            val item = iter.next()
            when {
                item.error is NodeError.NoSuchFile -> iter.remove()
                item.isCached -> iter.set(item.copy(children = null))
            }
        }
    }

    fun NodeProperties.isFile(): Boolean = access.firstOrNull() == FILE_CHAR

    fun NodeProperties.isDirectory(): Boolean = access.firstOrNull() == DIR_CHAR

    fun NodeProperties.isLink(): Boolean = access.firstOrNull() == LINK_CHAR

    fun Node.isDot(): Boolean = path.endsWith("/.")

    fun Node.withoutDot(): String = path.replace(Regex("(?<=\\/)\\.$"), "")

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
            output.success -> rename(name)
            else -> copy(error = output.error.toNodeError(path))
        }
    }

    private fun String.toNodeError(path: String): NodeError {
        val lines = split(NEW_LINE).filter { it.isNotBlank() }
        val first = lines.firstOrNull()
        return when {
            lines.size > 1 -> NodeError.Multiply
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

    private fun String.resolveFileType(content: NodeContent? = null, config: CacheConfig? = null): NodeContent = when {
        endsWith(EXT_PNG, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Picture.Png() }
        endsWith(EXT_JPG, ignoreCase = true) ||
        endsWith(EXT_JPEG, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Picture.Jpeg() }
        endsWith(EXT_GIF, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Picture.Gif() }
        endsWith(EXT_WEBP, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Picture.Webp() }
        endsWith(EXT_APK, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Apk() }
        endsWith(EXT_ZIP, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Archive.Zip() }
        endsWith(EXT_TAR, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Archive.Tar() }
        endsWith(EXT_BZ2, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Archive.Bzip2() }
        endsWith(EXT_GZ, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Archive.Gz() }
        endsWith(EXT_RAR, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Archive.Rar() }
        endsWith(EXT_SH, ignoreCase = true) -> NodeContent.File.Text.Script
        endsWith(EXT_HTML, ignoreCase = true) ||
        endsWith(EXT_TXT, ignoreCase = true) -> NodeContent.File.Text.Plain
        endsWith(EXT_IMG, ignoreCase = true) -> NodeContent.File.DataImage
        endsWith(EXT_MP4, ignoreCase = true) ||
        endsWith(EXT_3GP, ignoreCase = true) ||
        endsWith(EXT_AVI, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Movie() }
        endsWith(EXT_MP3, ignoreCase = true) ||
        endsWith(EXT_OGG, ignoreCase = true) ||
        endsWith(EXT_WAV, ignoreCase = true) ||
        endsWith(EXT_FLAC, ignoreCase = true) ||
        endsWith(EXT_OGA, ignoreCase = true) ||
        endsWith(EXT_AAC, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Music() }
        endsWith(EXT_PDF, ignoreCase = true) -> content.ifEmpty { NodeContent.File.Pdf }
        else -> NodeContent.File.Other
    }
}
