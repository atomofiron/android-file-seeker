package app.atomofiron.searchboxapp.utils

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore.Images.Thumbnails.MICRO_KIND
import android.provider.MediaStore.Images.Thumbnails.MINI_KIND
import android.util.Size
import app.atomofiron.common.util.Android
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.logE
import app.atomofiron.searchboxapp.model.CacheConfig
import java.io.File

private fun CacheConfig.kind() = if (legacySizeBig) MINI_KIND else MICRO_KIND

fun String.createAudioThumbnail(config: CacheConfig): Bitmap? = try {
    when {
        Android.Q -> ThumbnailUtils.createAudioThumbnail(File(this), config.thumbnailSize.let { Size(it, it) }, null)
        else -> ThumbnailUtils.createAudioThumbnail(this, config.kind())
    }
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
