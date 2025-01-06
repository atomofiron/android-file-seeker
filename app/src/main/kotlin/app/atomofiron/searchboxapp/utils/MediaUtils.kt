package app.atomofiron.searchboxapp.utils

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.util.Size
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.logE
import app.atomofiron.searchboxapp.model.CacheConfig
import java.io.File

fun String.createImageThumbnail(config: CacheConfig): Bitmap? = try {
    /* ThumbnailUtils.createImageThumbnail already do that
    val exif = ExifInterface(this)
    if (exif.rotationDegrees != 0) {
        val matrix = Matrix()
        matrix.setRotate(exif.rotationDegrees.toFloat())
        val recycle = thumbnail
        thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.width, thumbnail.height, matrix, false)
        recycle.recycle()
    }*/
    ThumbnailUtils.createImageThumbnail(File(this), config.thumbnailSize.let { Size(it, it) }, null)
} catch (e: Exception) {
    e.print(this)
    null
}

fun String.createVideoThumbnail(config: CacheConfig): Bitmap? = try {
    ThumbnailUtils.createVideoThumbnail(File(this), config.thumbnailSize.let { Size(it, it) }, null)
} catch (e: Exception) {
    e.print(this)
    null
}

fun String.createAudioThumbnail(config: CacheConfig): Bitmap? = try {
    ThumbnailUtils.createAudioThumbnail(File(this), config.thumbnailSize.let { Size(it, it) }, null)
} catch (e: Exception) {
    e.print(this)
    null
}

private fun Exception.print(path: String) {
    val message = when {
        BuildConfig.DEBUG -> "$path $this"
        else -> toString()
    }
    logE(message)
}
