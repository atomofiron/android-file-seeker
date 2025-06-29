package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.unsafeLazy
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail

@Suppress("DataClassPrivateConstructor")
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
    open val isCached = true

    data object Undefined : NodeContent()
    data object Link : NodeContent()

    data class Directory(
        val type: Type = Type.Ordinary,
        override val rootType: NodeRoot.NodeRootType? = null,
    ) : NodeContent() {
        enum class Type {
            Ordinary, Android, Camera, Download, Movies, Music, Pictures,
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
        override val thumbnail: Thumbnail,
        val duration: Int = 0, // todo
    ) : File() {
        override val isCached = duration >= 0
        constructor(path: String) : this(Thumbnail(path))
    }

    data class Music(
        override val thumbnail: Thumbnail? = null,
        val duration: Int = 0, // todo
    ) : File(mimeType = "audio/*") {
        override val isCached = duration >= 0
    }

    data class Picture private constructor(
        override val thumbnail: Thumbnail,
        override val mimeType: String,
        override val description: String? = null,
        override val details: String? = "", // todo
    ) : File(mimeType) {
        companion object {
            fun png(path: String, description: String? = null) = Picture(Thumbnail(path), mimeType = "image/png", description = description)
            fun apng(path: String, description: String? = null) = Picture(Thumbnail(path), mimeType = "image/apng", description = description)
            fun jpeg(path: String, description: String? = null) = Picture(Thumbnail(path), mimeType = "image/jpeg", description = description)
            fun gif(path: String, description: String? = null) = Picture(Thumbnail(path), mimeType = "image/gif", description = description)
            fun webp(path: String) = Picture(Thumbnail(path), mimeType = "image/webp")
            fun avif(path: String) = Picture(Thumbnail(path), mimeType = "image/avif")
        }
        override val isCached = details != null
    }

    sealed class Archive(mimeType: String) : File(mimeType) {
        open val children: List<Node>? = null
        override val isCached get() = children != null
    }

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
        override val children: List<Node>? = null,
    ) : Archive(mimeType = if (splitApk) "application/zip" else "application/vnd.android.package-archive") {

        override val details: String? = info?.versionName

        override val isCached = thumbnail?.ready == true

        override val thumbnail: Thumbnail? get() = when (info) {
            null -> Thumbnail.Loading
            else -> info.icon
        }
        companion object {
            fun apk(path: String, children: List<Node>? = null) = apk(NodeRef(path), children = children)
            fun apks(path: String, children: List<Node>? = null) = apks(NodeRef(path), children = children)
            fun apk(ref: NodeRef, info: ApkInfo? = null, children: List<Node>? = null) = AndroidApp(ref, splitApk = false, info, children)
            fun apks(ref: NodeRef, info: ApkInfo? = null, children: List<Node>? = null) = AndroidApp(ref, splitApk = true, info, children)
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
        data object Osu : Text("application/x-osu-beatmap")
        data object Svg : Text("image/svg+xml")
        data object Cpp : Text("text/x-c++src")
        data object Ino : Text("text/x-arduino")
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

fun NodeContent.isPicture(): Boolean = this is NodeContent.Picture

fun NodeContent.isMovie(): Boolean = this is NodeContent.Movie

fun NodeContent.isMedia(): Boolean = isPicture() || isMovie()
