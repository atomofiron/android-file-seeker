package app.atomofiron.searchboxapp.model.explorer

import android.graphics.Bitmap

sealed class NodeContent(
    // '*/*' - значит тип неизвестен,
    // null - пока неизвестно, известен тип или нет,
    // поэтому тут null
    val mimeType: String? = null,
) {

    object Unknown : NodeContent()
    object Link : NodeContent()

    data class Directory(val type: Type = Type.Ordinary) : NodeContent() {
        enum class Type {
            Ordinary, Android, Camera, Download, Movies, Music, Pictures,
        }
    }

    sealed class File(
        mimeType: String? = null,
        val thumbnail: Bitmap? = null,
    ) : NodeContent(mimeType) {
        // прямая связь
        val isEmpty: Boolean = thumbnail == null

        data class Movie(val duration: Int = 0, val preview: Bitmap? = null) : File()
        data class Music(val duration: Int = 0, val cover: Bitmap? = null) : File()
        sealed class Picture(
            mimeType: String,
            thumbnail: Bitmap? = null,
        ) : File(mimeType, thumbnail) {
            class Png(thumbnail: Bitmap? = null) : Picture("image/png", thumbnail)
            class Jpeg(thumbnail: Bitmap? = null) : Picture("image/jpeg", thumbnail)
            class Gif(thumbnail: Bitmap? = null) : Picture("image/gif", thumbnail)
            class Webp(thumbnail: Bitmap? = null) : Picture("image/webp", thumbnail)
        }
        data class Apk(
            val icon: Bitmap? = null,
            val versionName: String = "",
            val versionCode: Int = 0,
            val children: List<Node>? = null,
        ) : File("application/vnd.android.package-archive")
        sealed class Archive(
            mimeType: String,
            val children: List<Node>? = null,
        ) : File(mimeType) {
            class Zip(children: List<Node>? = null) : Archive("application/zip", children)
            class Bzip2(children: List<Node>? = null) : Archive("application/x-bzip2", children)
            class Gz(children: List<Node>? = null) : Archive("application/gzip", children)
            class Tar(children: List<Node>? = null) : Archive("application/x-tar", children)
            class Rar(children: List<Node>? = null) : Archive("application/vnd.rar", children)
        }
        sealed class Text : File("text/plain") {
            object Plain : Text()
            object Script : Text()
        }
        object Pdf : File("application/pdf")
        object DB : File()
        object DataImage : File()
        object Other : File()
        object Unknown : File()
    }
}
