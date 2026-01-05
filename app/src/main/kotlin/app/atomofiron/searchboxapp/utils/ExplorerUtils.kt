package app.atomofiron.searchboxapp.utils

import android.content.pm.PackageManager
import app.atomofiron.common.util.MutableList
import app.atomofiron.common.util.extension.debugFail
import app.atomofiron.common.util.extension.logE
import app.atomofiron.common.util.extension.takeIfDebug
import app.atomofiron.common.util.forHumans
import app.atomofiron.common.util.property.MutableWeakProperty
import app.atomofiron.searchboxapp.android.NativeBridge
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeContent.AndroidApp
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeId
import app.atomofiron.searchboxapp.model.explorer.NodeMeta
import app.atomofiron.searchboxapp.model.explorer.NodeOperation
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.NodeRootType
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.explorer.NodeStateImpl
import app.atomofiron.searchboxapp.model.explorer.other.DirectoryKind
import app.atomofiron.searchboxapp.utils.Const.LF
import kotlinx.coroutines.Job
import uniffi.native_lib.CommonProgress
import uniffi.native_lib.CountingResult
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
    // todo replace with enum in rust
    private const val NO_SUCH_FILE_OR_DIR = "No such file or directory"
    private const val PERMISSION_DENIED = "Permission denied"
    private const val RESOURCE_BUSY = "Device or resource busy"

    private const val DIRECTORY = NodeContent.Directory.MIME_TYPE
    private const val FILE_PICTURE = "image/"
    private const val FILE_AUDIO = "audio/"
    private const val FILE_VIDEO = "video/"
    private const val FILE_TEXT_SCRIPT = "text/x-"
    private const val FILE_TEXT = "text/"
    private const val FILE_SSA = "text/x-ssa" // .ass
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
    private const val FILE_TTF = "font/ttf"
    private const val FILE_XRIFF = "application/x-riff" // +webp
    // 'xml' unknown type: application/x-dia-shape

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
    private const val EXT_GIT = ".gitignore"
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
    private const val EXT_MKA = ".mka"
    private const val EXT_ASS = ".ass"
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
    private const val EXT_RFID = ".rfid"
    private const val EXT_SUB = ".sub"
    private const val EXT_IR = ".ir"
    private const val EXT_NFC = ".nfc"
    private const val EXT_IBTN = ".ibtn"
    private const val EXT_PDF = ".pdf"
    private const val EXT_PEM = ".pem"
    private const val EXT_P12 = ".p12"
    private const val EXT_CRT = ".crt"
    private const val EXT_TORRENT = ".torrent"
    private const val EXT_TTF = ".ttf"
    private const val EXT_OTF = ".otf"
    private const val EXT_EXE = ".exe"
    private const val EXT_XPI = ".xpi" // Mozilla extension
    private const val EXT_OSZ = ".osz" // osu map
    private const val EXT_OSK = ".osk" // osu skin
    private const val EXT_OSU = ".osu" // osu beatmap level
    private const val EXT_OLZ = ".olz" // osu lazer map
    private const val EXT_OSR = ".osr" // osu replay
    private const val EXT_OSB = ".osb" // osu storyboard

    suspend fun copy(from: Node, to: Node, move: Boolean, asSu: Boolean, collector: (CommonProgress) -> Unit): Node? {
        val result = NativeBridge.copy(from.ref, to.ref, move = move, asSu = asSu, collector)
        return to.apply(result)?.update(asSu, ensureCached = false)
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
        return Node(ref = target, parentRef = parent.ref, rootId = parent.rootId, meta = meta.toNodeMeta(), content = content)
    }

    fun NodeRef.toRoot(type: NodeRootType): Node {
        return Node(
            ref = this,
            meta = NodeMeta(),
            content = NodeContent.Directory(rootType = type),
        )
    }

    fun Meta.toNodeMeta(
        length: Long? = null,
        size: String? = null,
    ) = NodeMeta(
        access = access,
        owner = owner,
        group = group,
        date = date,
        time = time,
        length = when {
            length != null -> length
            access.firstOrNull() == FILE_CHAR -> this.length.toLong()
            else -> 0
        },
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

    private fun parse(ref: NodeRef, parentRef: NodeRef, root: Int, meta: NodeMeta): Node {
        val content = when (meta.access.firstOrNull()) {
            DIR_CHAR -> NodeContent.Directory(DirectoryKind.Ordinary)
            LINK_CHAR -> NodeContent.Link
            null -> NodeContent.Unknown
            else -> resolveFileType(ref)
        }
        return Node(
            rootId = root,
            ref = ref,
            parentRef = parentRef,
            meta = meta,
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

    suspend fun Node.update(asSu: Boolean, ensureCached: Boolean = true): Node {
        val type = NativeBridge.type(ref, asSu)
        return when (type) {
            is Rslt.Ok -> parseNode(type.value.meta).resolveType(type.value.mime)
                .run { if (ensureCached) ensureCached(asSu, oldProps = meta) else this }
            is Rslt.Err -> copy(error = type.message.toNodeError())
        }
    }

    fun Node.updateUsage(asSu: Boolean): NodeMeta {
        if (!isDirectory) {
            return meta
        }
        val result = NativeBridge.usage(ref, asSu)
        val (length, size) = when (result) {
            is Rslt.Err -> NodeMeta.Empty.run { length to size }
            is Rslt.Ok -> result.value
        }
        if (length == this.length && size == this.size) {
            return meta
        }
        return meta.copy(length = length, size = size)
    }

    private suspend fun Node.ensureCached(asSu: Boolean, oldProps: NodeMeta): Node = when {
        isDirectory -> cacheDir(asSu)
        length == 0L && oldProps.size != size -> resolveFileType()
        length == 0L -> this
        isCached && oldProps.size == size -> this
        // if size changed -> cache again
        else -> try {
            cacheFile(asSu)
        } catch (e: Exception) {
            this.copy(error = NodeError.Message(e.toString()))
        }
    }

    fun Node.cacheDir(asSu: Boolean): Node {
        val result = NativeBridge.metas(ref, asSu)
        return when (result) {
            is Rslt.Ok -> parseDir(result.value)
            is Rslt.Err -> copy(error = result.message.toNodeError())
        }
    }

    /** resolve content types */
    fun Node.resolveDirChildren(asSu: Boolean): Boolean {
        val children = children ?: return false
        val entries = when (val types = NativeBridge.types(ref, asSu)) {
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
                    .copy(meta = entry.meta.toNodeMeta(length = child.length, size = child.size))
            }
        }
        return entries.isNotEmpty()
    }

    fun Node.resolveType(mimeType: String): Node = copy(content = ref.resolveContent(mimeType, meta, content))

    fun NodeRef.resolveContent(mimeType: String, meta: NodeMeta, content: NodeContent? = null): NodeContent {
        return when (true) {
            (meta.access.firstOrNull() == DIR_CHAR),
            (mimeType == DIRECTORY),
            (content is NodeContent.Directory) -> content.ifMismatches { NodeContent.Directory() }
            (meta.length == 0L) -> NodeContent.Empty
            mimeType.isBlank(),
            (mimeType == FILE_UNKNOWN) -> content.resolveFileType(this)
            mimeType.startsWith(FILE_PICTURE) -> content.ifMismatches { NodeContent.Picture.resolve(mimeType) }
            mimeType.startsWith(FILE_TEXT) -> when {
                name.hasExt(EXT_SVG) -> content.ifMismatches { NodeContent.Text.Svg }
                name.hasExt(EXT_NFC) -> content.ifMismatches { NodeContent.Text.Nfc }
                name.hasExt(EXT_IR) -> content.ifMismatches { NodeContent.Text.Ir }
                name.hasExt(EXT_IBTN) -> content.ifMismatches { NodeContent.Text.Ibtn }
                name.hasExt(EXT_SUB) -> content.ifMismatches { NodeContent.Text.Sub }
                name.hasExt(EXT_RFID) -> content.ifMismatches { NodeContent.Text.Rfid }
                name == EXT_GIT -> content.ifMismatches { NodeContent.Text.Gitignore }
                name.hasExt(EXT_OSU) -> content.ifMismatches { NodeContent.Text.Osu }
                name.hasExt(EXT_CPP) -> content.ifMismatches { NodeContent.Text.Cpp }
                name.hasExt(EXT_INO) -> content.ifMismatches { NodeContent.Text.Ino }
                name.hasExt(EXT_BAT) -> content.ifMismatches { NodeContent.Text.BatScript }
                mimeType == FILE_SSA -> content.ifMismatches { NodeContent.Text.Subtitles }
                else -> NodeContent.Text.Plain
            }
            (mimeType == FILE_XRIFF) -> content.ifMismatches { NodeContent.Picture(mimeType) }
            (mimeType == FILE_APK) -> content.ifMismatches { AndroidApp.apk(this) }
            (mimeType == FILE_RAR) -> content.ifMismatches { NodeContent.Rar() }
            (mimeType == FILE_ZIP) -> when (true) {
                name.hasExt(EXT_APKS),
                name.hasExt(EXT_APKM) -> content.ifMismatches { AndroidApp.apks(this) }
                (content is AndroidApp) -> return content
                name.hasExt(EXT_OSZ) -> content.ifMismatches { NodeContent.Osu.Map() }
                name.hasExt(EXT_OSK) -> content.ifMismatches { NodeContent.Osu.Skin() }
                name.hasExt(EXT_OLZ) -> content.ifMismatches { NodeContent.Osu.LazerMap() }
                name.hasExt(EXT_OSR) -> content.ifMismatches { NodeContent.Osu.Replay() }
                name.hasExt(EXT_OSB) -> content.ifMismatches { NodeContent.Osu.Storyboard() }
                else -> content.ifMismatches { NodeContent.Zip() }
            }
            (mimeType == FILE_BZIP2) -> when {
                name.hasExt(EXT_DMG) -> content.ifMismatches { NodeContent.Dmg }
                else -> content.ifMismatches { NodeContent.Bzip2() }
            }
            (mimeType == FILE_GZIP) -> content.ifMismatches { NodeContent.Gz() }
            (mimeType == FILE_TAR) -> content.ifMismatches { NodeContent.Tar() }
            (mimeType == FILE_XZ) -> content.ifMismatches { NodeContent.Xz }
            mimeType.startsWith(FILE_FLASH) -> content.ifMismatches { NodeContent.Flash }
            mimeType.startsWith(FILE_EXE) -> content.ifMismatches { NodeContent.ExeMs }
            mimeType.startsWith(FILE_MESSAGE) -> NodeContent.Text.Plain
            (mimeType == FILE_XML) -> content.ifMismatches { NodeContent.Text.Xml }
            mimeType.startsWith(FILE_AUDIO) -> content.ifMismatches { NodeContent.Music.resolve(mimeType) }
            mimeType.startsWith(FILE_VIDEO),
            (mimeType == FILE_MATROSKA) -> when {
                name.hasExt(EXT_MKA) -> content.ifMismatches { NodeContent.Music.resolve(mimeType) }
                else -> content.ifMismatches { NodeContent.Movie.resolve(mimeType) }
            }
            (mimeType == FILE_PDF) -> content.ifMismatches { NodeContent.Pdf }
            (mimeType == FILE_ELF_EXE) -> when {
                name.hasExt(EXT_ODEX) -> content.ifMismatches { NodeContent.Java }
                else -> content.ifMismatches { NodeContent.Elf }
            }
            (mimeType == FILE_ELF_RE) -> when {
                name.hasExt(EXT_FAP) -> content.ifMismatches { NodeContent.Fap }
                else -> content.ifMismatches { NodeContent.Elf }
            }
            (mimeType == FILE_PEM),
            (mimeType == FILE_CERT),
            (mimeType == FILE_CA_CERT) -> content.ifMismatches { NodeContent.Cert }
            (mimeType == FILE_TORRENT) -> content.ifMismatches { NodeContent.Torrent }
            (mimeType == FILE_ODT) -> content.ifMismatches { NodeContent.Document }
            (mimeType == FILE_TTF) -> content.ifMismatches { NodeContent.Font }
            (mimeType == FILE_ELF_SO) -> content.ifMismatches { NodeContent.ElfSo }
            (mimeType == FILE_MS_EXE) -> content.ifMismatches { NodeContent.ExeMs }
            (mimeType == FILE_APL_EXE) -> content.ifMismatches { NodeContent.ExeApl }
            (mimeType == FILE_JAVA) -> content.ifMismatches { NodeContent.Java }
            (mimeType == FILE_SCRIPT),
            mimeType.startsWith(FILE_TEXT_SCRIPT) -> NodeContent.Text.ShellScript
            else -> {
                logE("'$ext' unknown type: $mimeType ${takeIfDebug()}")
                content.resolveFileType(this)
            }
        }
    }

    private suspend fun Node.cacheFile(asSu: Boolean): Node {
        val content = when (content) {
            is NodeContent.Picture,
            is NodeContent.Movie -> content
            is NodeContent.Music -> content
            is NodeContent.Zip -> cacheZip().let { item ->
                when (children?.possibleContainsMainApk()) {
                    null, false -> return item
                    true -> AndroidApp.apks(ref).let { apks ->
                        apks.getAppContent(asSu)
                            .contentOrNodeError(this, apks.copy(isCached = true)) { return it }
                    }
                }
            }
            is AndroidApp -> content.getAppContent(asSu)
                .contentOrNodeError(this, content.copy(isCached = true)) { return it }
            else -> return this
        }
        return copy(content = content)
    }

    private inline fun <C : NodeContent> Rslt<C>.contentOrNodeError(node: Node, content: NodeContent, action: (withError: Node) -> Nothing): C {
        return unwrapOrElse {
            action(node.copy(content = content, error = NodeError.Message.orUnknown(it)))
        }
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
                val dateTime = SimpleDateFormat(NodeMeta.DATE_TIME_FORMAT, Locale.ROOT)
                    .format(Date(entry.time))
                    .split(NodeMeta.DATE_TIME_SEPARATOR)
                val meta = NodeMeta(date = dateTime.first(), time = dateTime.last(), size = entry.size.toSize(), length = entry.size)
                val child = Node(ref + entry.name, parentRef = ref, rootId = uniqueId, meta = meta, content = content)
                children.add(child)
                entry = stream.nextEntry
            }
        }
        val content = (content as NodeContent.Zip).copy(isCached = true)
        copy(children = NodeChildren(children), content = content)
    } catch (e: Exception) {
        copy(error = NodeError.Message(e.forHumans()))
    }

    private inline fun <reified T : NodeContent> NodeContent?.ifMismatches(action: () -> T): T = this as? T ?: action()

    fun Node.sortBy(how: NodeSorting): Node {
        children?.sortBy(how)
        return this
    }

    fun NodeChildren.sortBy(how: NodeSorting) = items.sortBy(how) { it }

    fun <T> MutableList<T>.sortBy(how: NodeSorting, what: (T) -> Node) = when (how) {
        is NodeSorting.Size -> sortBySize(what, reversed = how.reversed)
        is NodeSorting.Name -> sortByName(what, reversed = how.reversed)
        is NodeSorting.Date -> sortByDate(what, reversed = how.reversed)
    }

    fun <T> MutableList<T>.sortByName(what: (T) -> Node, reversed: Boolean) {
        sortBy(reversed) { (what(it)).lowercaseName }
        sortBy { !what(it).isDirectory }
    }

    private fun <T> MutableList<T>.sortBySize(what: (T) -> Node, reversed: Boolean) {
        sortBy { what(it).lowercaseName }
        sortBy(reversed) { what(it).length }
        sortBy { !what(it).isDirectory }
    }

    private fun <T> MutableList<T>.sortByDate(what: (T) -> Node, reversed: Boolean) {
        sortBy { what(it).lowercaseName }
        sortBy(reversed) { what(it).time }
        sortBy(reversed) { what(it).date }
        sortBy { !what(it).isDirectory }
    }

    private fun Node.parseNode(meta: Meta): Node {
        val meta = meta.toNodeMeta(length, size)
        val (children, content) = when {
            meta.isDirectory() -> when (content) {
                is NodeContent.Directory -> children to content
                else -> null to NodeContent.Directory()
            }
            meta.isLink() -> when (content) {
                is NodeContent.Link -> children to content
                else -> null to NodeContent.Link
            }
            meta.isFile() -> when (content) {
                is NodeContent.File -> children to content
                else -> null to resolveFileType(ref)
            }
            else -> null to NodeContent.Undefined
        }
        return copy(children = children, meta = meta, content = content)
    }

    private fun Node.parseDir(metas: List<Meta>): Node {
        val items = MutableList<Node>(metas.size)
        val files = MutableList<Node>(metas.size)
        for (i in metas.indices) {
            val m = metas[i]
            var meta = m.toNodeMeta()
            val parentRef = this@parseDir.ref
            val ref = NodeRef(m.path)
            val child = children?.findOnMut { it.ref == ref }
            if (child?.isDirectory == true) {
                meta = meta.copy(length = child.meta.length, size = child.meta.size)
            }
            val item = when {
                child == null -> parse(ref, parentRef, rootId, meta)
                child.meta == meta -> child
                else -> child.copy(meta = meta)
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

    fun NodeMeta.isFile(): Boolean = access.firstOrNull() == FILE_CHAR

    fun NodeMeta.isDirectory(): Boolean = access.firstOrNull() == DIR_CHAR

    fun NodeMeta.isLink(): Boolean = access.firstOrNull() == LINK_CHAR

    fun NodeRef.isContent() = string.startsWith(Const.SCHEME_CONTENT)

    fun NodeRef.toNode(
        rootId: NodeId = uniqueId,
        parentRef: NodeRef = parent,
        content: NodeContent = NodeContent.Undefined,
        meta: NodeMeta = NodeMeta.Empty,
        children: NodeChildren? = null,
    ) = Node(this, parentRef = parentRef, meta = meta, rootId = rootId, content = content, children = children)

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
        val result = NativeBridge.copy(from = ref, to = targetRef, asSu = asSu, move = true) { }
        return apply(result)
    }

    fun Node.apply(result: CountingResult): Node? {
        val rMeta = when (result) {
            is CountingResult.Ok -> result.meta
            is CountingResult.Err -> result.v1
        }
        rMeta ?: return null
        val ref = when {
            ref.theSame(rMeta.path) -> ref
            else -> NodeRef(rMeta.path)
        }
        val meta = when (ref) {
            this.ref -> rMeta.toNodeMeta(length, size)
            else -> rMeta.toNodeMeta()
        }
        val error = rMeta.error
            ?.toNodeError()
            ?: (result as? CountingResult.Ok)
                ?.errors
                ?.toNodeError()
        return mutate(ref = ref, parentRef = ref.parent, meta = meta, error = error)
    }

    fun Node.move(parent: NodeRef = parentRef, name: String = this.name): Node {
        val ref = parent + name
        return mutate(ref = ref, parentRef = parent, meta = meta)
    }

    fun String.toNodeError(): NodeError {
        val lines = split(LF).filter { it.isNotBlank() }
        val first = lines.firstOrNull()
        return when {
            lines.size > 1 -> NodeError.Multiply(lines)
            first == null -> NodeError.Unknown
            first.startsWith(NO_SUCH_FILE_OR_DIR) -> NodeError.NoSuchFileOrDir
            first.startsWith(PERMISSION_DENIED) -> NodeError.PermissionDenied
            first.startsWith(RESOURCE_BUSY) -> NodeError.ResourceBusy
            else -> NodeError.Message.orUnknown(first)
        }
    }

    fun Node.isInaccessible() = error is NodeError.NoSuchFileOrDir || error is NodeError.PermissionDenied

    private fun List<String>.toNodeError(): NodeError? = takeIf { it.isNotEmpty() }
        ?.let { NodeError.Multiply(it) }

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
        ref.name.hasExt(EXT_APNG) -> ifMismatches { NodeContent.Picture.Apng }
        ref.name.hasExt(EXT_PNG) -> ifMismatches { NodeContent.Picture.Png }
        ref.name.hasExt(EXT_JPG),
        ref.name.hasExt(EXT_JPEG) -> ifMismatches { NodeContent.Picture.Jpeg }
        ref.name.hasExt(EXT_GIF) -> ifMismatches { NodeContent.Picture.Gif }
        ref.name.hasExt(EXT_WEBP) -> ifMismatches { NodeContent.Picture.Webp }
        ref.name.hasExt(EXT_AVIF) -> ifMismatches { NodeContent.Picture.Avif }
        ref.name.hasExt(EXT_APK) -> ifMismatches { AndroidApp.apk(ref) }
        ref.name.hasExt(EXT_DEX),
        ref.name.hasExt(EXT_ODEX),
        ref.name.hasExt(EXT_VDEX) -> ifMismatches { NodeContent.Java }
        ref.name.hasExt(EXT_APKS),
        ref.name.hasExt(EXT_APKM) -> ifMismatches { AndroidApp.apks(ref) }
        ref.name.hasExt(EXT_ZIP),
        ref.name.hasExt(EXT_XAPK) -> ifMismatches { NodeContent.Zip() }
        ref.name.hasExt(EXT_TAR) -> ifMismatches { NodeContent.Tar() }
        ref.name.hasExt(EXT_BZ2) -> ifMismatches { NodeContent.Bzip2() }
        ref.name.hasExt(EXT_GZ) -> ifMismatches { NodeContent.Gz() }
        ref.name.hasExt(EXT_RAR) -> ifMismatches { NodeContent.Rar() }
        ref.name.hasExt(EXT_SH) -> NodeContent.Text.ShellScript
        ref.name.hasExt(EXT_BAT) -> NodeContent.Text.BatScript
        ref.name.hasExt(EXT_TXT),
        ref.name.hasExt(EXT_GIT),
        ref.name.hasExt(EXT_INI),
        ref.name.hasExt(EXT_JAVA),
        ref.name.hasExt(EXT_KT),
        ref.name.hasExt(EXT_KTS),
        ref.name.hasExt(EXT_SWIFT),
        ref.name.hasExt(EXT_YAML),
        ref.name.hasExt(EXT_HTML) -> NodeContent.Text.Plain
        ref.name.hasExt(EXT_SVG) -> ifMismatches { NodeContent.Text.Svg }
        ref.name.hasExt(EXT_IMG) -> NodeContent.DataImage
        ref.name.hasExt(EXT_MP4) -> ifMismatches { NodeContent.Movie.Mp4 }
        ref.name.hasExt(EXT_MKV) -> ifMismatches { NodeContent.Movie.Mkv }
        ref.name.hasExt(EXT_MOV) -> ifMismatches { NodeContent.Movie.Mov }
        ref.name.hasExt(EXT_WEBM) -> ifMismatches { NodeContent.Movie.Webm }
        ref.name.hasExt(EXT_3GP) -> ifMismatches { NodeContent.Movie.Tgp }
        ref.name.hasExt(EXT_AVI) -> ifMismatches { NodeContent.Movie.Avi }
        ref.name.hasExt(EXT_MP3) -> ifMismatches { NodeContent.Music.Mp3 }
        ref.name.hasExt(EXT_M4A) -> ifMismatches { NodeContent.Music.M4a }
        ref.name.hasExt(EXT_OGA),
        ref.name.hasExt(EXT_OGG) -> ifMismatches { NodeContent.Music.Ogg }
        ref.name.hasExt(EXT_WAV) -> ifMismatches { NodeContent.Music.Wav }
        ref.name.hasExt(EXT_FLAC) -> ifMismatches { NodeContent.Music.Flac }
        ref.name.hasExt(EXT_AAC) -> ifMismatches { NodeContent.Music.Aac }
        ref.name.hasExt(EXT_PDF) -> ifMismatches { NodeContent.Pdf }
        ref.name.hasExt(EXT_TORRENT) -> ifMismatches { NodeContent.Torrent }
        ref.name.hasExt(EXT_FAP) -> ifMismatches { NodeContent.Fap }
        ref.name.hasExt(EXT_EXE) -> ifMismatches { NodeContent.ExeMs }
        ref.name.hasExt(EXT_SWF) -> ifMismatches { NodeContent.Flash }
        ref.name.hasExt(EXT_TTF),
        ref.name.hasExt(EXT_OTF) -> ifMismatches { NodeContent.Font }
        ref.name.hasExt(EXT_PEM),
        ref.name.hasExt(EXT_P12),
        ref.name.hasExt(EXT_CRT) -> ifMismatches { NodeContent.Cert }
        ref.name.hasExt(EXT_OSZ) -> ifMismatches { NodeContent.Osu.Map() }
        ref.name.hasExt(EXT_OSK) -> ifMismatches { NodeContent.Osu.Skin() }
        ref.name.hasExt(EXT_OLZ) -> ifMismatches { NodeContent.Osu.LazerMap() }
        ref.name.hasExt(EXT_OSR) -> ifMismatches { NodeContent.Osu.Replay() }
        ref.name.hasExt(EXT_OSB) -> ifMismatches { NodeContent.Osu.Storyboard() }
        ref.name.hasExt(EXT_XPI) -> ifMismatches { NodeContent.Firefox }
        ref.name.hasExt(EXT_ASS) -> ifMismatches { NodeContent.Text.Subtitles }
        else -> NodeContent.Other
    }

    fun Node.updateWith(item: Node): Node {
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
                    val actual = when (old.meta) {
                        next.meta -> old
                        else -> old.copy(meta = next.meta)
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
            meta = item.meta,
            content = content,
            children = children,
            error = item.error,
        )
    }

    fun Node.updateWith(new: NodeContent, meta: NodeMeta): Node {
        val content = when (true) {
            (new::class != content::class),
            !isCached -> new
            else -> null
        }
        val meta = when {
            meta == this.meta -> null
            !meta.isDirectory() -> meta
            meta.size.isNotEmpty() -> meta
            this.meta.size.isEmpty() -> meta
            else -> meta.copy(length = this.meta.length, size = this.meta.size)
        }
        return when (true) {
            (meta != null),
            (content != null) -> copy(content = content ?: this.content, meta = meta ?: this.meta)
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
                second.ref.isChildOf(first.ref) -> removeAt(j)
                first.ref.isChildOf(second.ref) -> removeAt(i)
                j == lastIndex -> j = ++i + 1
                else -> j++
            }
        }
    }

    private fun String.hasExt(ext: String) = endsWith(ext, ignoreCase = true)
}
