package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.unsafeLazy
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail

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
    open val rootType: NodeRoot.NodeRootType? = null
    open val isCached: Boolean = false

    data object Unknown : NodeContent()
    data object Link : NodeContent()

    data class Directory(
        val type: Type = Type.Ordinary,
        override val rootType: NodeRoot.NodeRootType? = null,
    ) : NodeContent() {
        enum class Type {
            Ordinary, Android, Camera, Download, Movies, Music, Pictures,
        }
        override val isCached: Boolean get() = rootType != null
    }

    sealed class File(
        mimeType: String? = null,
        open val thumbnail: Thumbnail? = null,
        override val details: String? = null,
    ) : NodeContent(mimeType) {
        // прямая связь
        val isEmpty: Boolean get() = thumbnail == null
        override val isCached: Boolean get() = thumbnail != null

        data class Movie(
            override val thumbnail: Thumbnail,
            val duration: Int = 0,
        ) : File() {
            override val isCached: Boolean = duration > -1
            constructor(path: String) : this(Thumbnail(path))
        }

        data class Music(
            val duration: Int = 0,
            override val thumbnail: Thumbnail? = null,
        ) : File(mimeType = "audio/*")

        sealed class Picture(
            mimeType: String,
            override val details: String? = "", // todo
        ) : File(mimeType) { // todo remove children

            override val isCached: Boolean = details != null

            data class Png(val path: String) : Picture("image/png") {
                override val thumbnail = Thumbnail(path)
            }
            data class Jpeg(val path: String) : Picture("image/jpeg") {
                override val thumbnail = Thumbnail(path)
            }
            data class Gif(val path: String) : Picture("image/gif") {
                override val thumbnail = Thumbnail(path)
            }
            data class Webp(val path: String) : Picture("image/webp") {
                override val thumbnail = Thumbnail(path)
            }
            data class Avif(val path: String) : Picture("image/avif") {
                override val thumbnail = Thumbnail(path)
            }
        }

        sealed class Archive(mimeType: String) : File(mimeType) {
            open val children: List<Node>? = null
        }

        /*sealed class Zip(
            override val children: List<Node>? = null,
            override val mimeType: String = "application/zip",
        ) : Archive(mimeType) {
            companion object {
                private data class Zip() : File.Zip()
                operator fun invoke() = Zip()
            }
        }*/
        data class Zip(
            override val children: List<Node>? = null,
            override val mimeType: String = "application/zip",
        ) : Archive(mimeType)

        data class Bzip2(override val children: List<Node>? = null) : Archive("application/x-bzip2")
        data class Gz(override val children: List<Node>? = null) : Archive("application/gzip")
        data class Tar(override val children: List<Node>? = null) : Archive("application/x-tar")
        data class Rar(override val children: List<Node>? = null) : Archive("application/vnd.rar")


        data class AndroidApp private constructor(
            val ref: NodeRef,
            val splitApk: Boolean,
            val info: ApkInfo? = null,
        ) : Archive(mimeType = if (splitApk) "application/zip" else "application/vnd.android.package-archive") {
            override val details: String? = info?.versionName
            override val thumbnail: Thumbnail? get() = info?.icon

            @Suppress("FunctionName")
            companion object {
                fun Apk(path: String) = Apk(NodeRef(path))
                fun Apks(path: String) = Apks(NodeRef(path))
                fun Apk(ref: NodeRef, info: ApkInfo? = null) = AndroidApp(ref, splitApk = false, info)
                fun Apks(ref: NodeRef, info: ApkInfo? = null) = AndroidApp(ref, splitApk = true, info)
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
            data object Script : Text()
            data object Osu : Text("application/x-osu-beatmap")
            data object Svg : Picture("image/svg+xml")
            data object Ino : Text()
        }
        data object Pdf : File("application/pdf")
        data object Torrent : File("application/x-bittorrent")
        data object Xz : File()
        data object DB : File()
        data object DataImage : File()
        data object Elf : File()
        data object Fap : File()
        data object ElfSo : File()
        data object ExeMs : File()
        data object ExeApl : File()
        data object ExeApls : File()
        data object Flash : File()
        data object Cert : File()
        data object Dmg : File()
        data object Other : File()
        data object Unknown : File()
    }
}

fun NodeContent.isPicture(): Boolean = this is NodeContent.File.Picture

fun NodeContent.isMovie(): Boolean = this is NodeContent.File.Movie

fun NodeContent.isMedia(): Boolean = isPicture() || isMovie()
