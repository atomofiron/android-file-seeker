package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.unsafeLazy
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import app.atomofiron.searchboxapp.model.explorer.other.DirectoryKind
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.utils.ExplorerUtils.FILE_APK
import app.atomofiron.searchboxapp.utils.ExplorerUtils.FILE_ZIP

sealed class NodeContent(
    // '*/*' - значит тип неизвестен,
    // null - пока неизвестно, известен тип или нет,
    // поэтому тут null
    open val mimeType: String? = null,
    open val details: String? = null,
) {
    companion object {
        const val AnyType = "*/*"
    }
    val commonMimeType: String by unsafeLazy { mimeType?.run { substring(0, indexOf('/')) + "/*" } ?: AnyType }
    open val rootType: NodeRootInfo? = null
    open val isCached = true

    data object Undefined : NodeContent() {
        override val isCached = false
    }

    data object Link : NodeContent()

    data class Directory(
        val kind: DirectoryKind = DirectoryKind.Ordinary, // always Ordinary in the garden
        override val rootType: NodeRootInfo? = null,
    ) : NodeContent(mimeType = MIME_TYPE) {
        companion object {
            const val MIME_TYPE = "inode/directory"
            val mimeTypes = listOf(MIME_TYPE)
        }
        override val isCached = rootType != null
    }

    sealed class File(
        mimeType: String? = null,
        open val thumbnail: Thumbnail? = null,
        open val description: String? = null,
        override val details: String? = null,
    ) : NodeContent(mimeType)

    data object Empty : File()

    data class Movie(
        override val mimeType: String,
        val duration: Int = 0, // todo
    ) : File(mimeType, thumbnail = Thumbnail.FilePath) {
        companion object {
            val Mp4 = Movie(mimeType = "video/mp4")
            val Mkv = Movie(mimeType = "video/x-matroska")
            val Amkv = Movie(mimeType = "application/x-matroska")
            val Avi = Movie(mimeType = "video/x-msvideo")
            val Mov = Movie(mimeType = "video/quicktime")
            val Webm = Movie(mimeType = "video/webm")
            val Tgp = Movie(mimeType = "video/3gpp")
            val Tgp2 = Movie(mimeType = "video/3gpp2")

            private val popular = listOf(Mp4, Mkv, Amkv, Avi, Mov, Webm, Tgp, Tgp2)
                .associateBy { it.mimeType }

            fun resolve(mimeType: String) = popular[mimeType] ?: Movie(mimeType = mimeType)
        }
        override val isCached = duration >= 0
    }

    @ConsistentCopyVisibility
    data class Music private constructor(
        override val mimeType: String,
        val duration: Int = 0, // todo
    ) : File(mimeType = mimeType, thumbnail = Thumbnail.FilePath) {
        companion object {
            val Mp3 = Music("audio/mpeg")
            val Aac = Music("audio/aac")
            val M4a = Music("audio/mp4")
            val Ogg = Music("audio/ogg")
            val Flac = Music("audio/flac")
            val Wma = Music("audio/x-ms-wma")
            val Wav = Music("audio/wav")
            val Xwav = Music("audio/x-wav")

            private val popular = listOf(Mp3, Aac, M4a, Ogg, Flac, Wma, Wav, Xwav)
                .associateBy { it.mimeType }

            fun resolve(mimeType: String) = popular[mimeType] ?: Music(mimeType = mimeType)
        }
        override val isCached = duration >= 0
    }

    data class Picture(
        override val mimeType: String,
        override val description: String? = null,
        override val details: String? = "", // todo
    ) : File(mimeType, Thumbnail.FilePath) {
        companion object {
            val Png = Picture(mimeType = "image/png")
            val Apng = Picture(mimeType = "image/apng")
            val Jpeg = Picture(mimeType = "image/jpeg")
            val Gif = Picture(mimeType = "image/gif")
            val Webp = Picture(mimeType = "image/webp")
            val Avif = Picture(mimeType = "image/avif")

            private val popular = listOf(Png, Apng, Jpeg, Gif, Webp, Avif)
                .associateBy { it.mimeType }

            fun resolve(mimeType: String) = popular[mimeType] ?: Picture(mimeType = mimeType)
        }
        override val isCached = details != null
    }

    sealed class Archive(mimeType: String) : File(mimeType)

    data class Zip(
        override val mimeType: String = FILE_ZIP,
        override val isCached: Boolean = false,
    ) : Archive(mimeType)

    data class Bzip2(override val isCached: Boolean = true) : Archive("application/x-bzip2")
    data class Gz(override val isCached: Boolean = true) : Archive("application/gzip")
    data class Tar(override val isCached: Boolean = true) : Archive("application/x-tar")
    data class Rar(override val isCached: Boolean = true) : Archive("application/vnd.rar")

    data class AndroidApp(
        val splitApk: Boolean,
        val info: ApkInfo? = null,
        override val isCached: Boolean = false,
    ) : Archive(mimeType = if (splitApk) FILE_ZIP else FILE_APK) {

        override val details: String? = info?.versionName

        override val thumbnail: Thumbnail? get() = when {
            info != null -> info.icon
            !isCached -> Thumbnail.Loading
            else -> null
        }
        companion object {
            val Apk = AndroidApp(splitApk = false)
            val Apks = AndroidApp(splitApk = true)
        }
    }

    sealed class Osu(
        mimeType: String,
    ) : File(mimeType) {
        abstract val children: List<Node>?

        data class Map(override val children: List<Node>? = null) : Osu("application/x-osu-beatmap-archive")
        data class Skin(override val children: List<Node>? = null) : Osu("application/x-osu-skin-archive")
        data class LazerMap(override val children: List<Node>? = null) : Osu("application/x-osu-beatmap-archive")
        data class Storyboard(override val children: List<Node>? = null) : Osu("application/x-osu-storyboard")
        data class Replay(override val children: List<Node>? = null) : Osu("application/x-osu-replay")
    }
    sealed class Text(mimeType: String = "text/plain") : File(mimeType) {
        data object Plain : Text()
        data object ShellScript : Text("text/x-shellscript")
        data object BatScript : Text()
        data object Subtitles : Text()
        data object Gitignore : Text()
        data object Ir : Text()
        data object Sub : Text()
        data object Rfid : Text()
        data object Nfc : Text()
        data object Ibtn : Text()
        data object Osu : Text("application/x-osu-beatmap")
        data object Svg : Text("image/svg+xml")
        data object Cpp : Text("text/x-c++src")
        data object Ino : Text("text/x-arduino")
        data object Xml : Text("application/xml")
    }
    data object Pdf : File("application/pdf")
    data object Torrent : File("application/x-bittorrent")
    data object Document : File()
    data object Xz : File()
    data object DB : File()
    data object DataImage : File()
    data object Elf : File()
    data object Fap : File()
    data object ElfSo : File()
    data object ExeMs : File()
    data object ExeApl : File()
    data object ExeApls : File()
    data object Java : File()
    data object Flash : File()
    data object Font : File()
    data object Cert : File()
    data object Dmg : File()
    data object Firefox : File()
    data object Unknown : File()

    fun matchesAny(mimeType: List<String>): Boolean {
        if (mimeType.isEmpty()) return true
        val current = this.mimeType ?: return false
        return mimeType.any {
            when (it.endsWith("/*")) {
                true -> current.startsWith(it.substring(0, it.length.dec()), ignoreCase = true)
                false -> current.equals(it, ignoreCase = true)
            }
        }
    }
}

fun NodeContent.isPicture(): Boolean = this is NodeContent.Picture

fun NodeContent.isMovie(): Boolean = this is NodeContent.Movie

fun NodeContent.isMedia(): Boolean = isPicture() || isMovie()
