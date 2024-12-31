package app.atomofiron.searchboxapp.model.explorer

sealed class NodeContent(
    // '*/*' - значит тип неизвестен,
    // null - пока неизвестно, известен тип или нет,
    // поэтому тут null
    val mimeType: String? = null,
) {
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
    }

    sealed class File(
        mimeType: String? = null,
        open val thumbnail: Thumbnail? = null,
    ) : NodeContent(mimeType) {
        // прямая связь
        val isEmpty: Boolean get() = thumbnail == null
        override val isCached: Boolean get() = thumbnail != null

        data class Movie(
            val duration: Int = 0,
            override val thumbnail: Thumbnail? = null,
        ) : File()
        data class Music(
            val duration: Int = 0,
            override val thumbnail: Thumbnail? = null,
        ) : File()
        sealed class Picture(mimeType: String) : File(mimeType) {
            data class Png(override val thumbnail: Thumbnail? = null) : Picture("image/png")
            data class Jpeg(override val thumbnail: Thumbnail? = null) : Picture("image/jpeg")
            data class Gif(override val thumbnail: Thumbnail? = null) : Picture("image/gif")
            data class Webp(override val thumbnail: Thumbnail? = null) : Picture("image/webp")
        }
        data class Apk(
            override val thumbnail: Thumbnail? = null,
            val appName: String = "",
            val versionName: String = "",
            val versionCode: Int = 0,
            val children: List<Node>? = null,
        ) : File("application/vnd.android.package-archive", thumbnail)
        sealed class Archive(
            mimeType: String,
        ) : File(mimeType) {
            abstract val children: List<Node>?

            data class Zip(override val children: List<Node>? = null) : Archive("application/zip")
            data class Bzip2(override val children: List<Node>? = null) : Archive("application/x-bzip2")
            data class Gz(override val children: List<Node>? = null) : Archive("application/gzip")
            data class Tar(override val children: List<Node>? = null) : Archive("application/x-tar")
            data class Rar(override val children: List<Node>? = null) : Archive("application/vnd.rar")
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
        }
        data object Pdf : File("application/pdf")
        data object DB : File()
        data object DataImage : File()
        data object Pem : File()
        data object Elf : File()
        data object ElfSo : File()
        data object ExeMs : File()
        data object ExeApl : File()
        data object ExeApls : File()
        data object Other : File()
        data object Unknown : File()
    }
}

fun NodeContent.isPicture(): Boolean = this is NodeContent.File.Picture

fun NodeContent.isMovie(): Boolean = this is NodeContent.File.Movie

fun NodeContent.isMedia(): Boolean = isPicture() || isMovie()
