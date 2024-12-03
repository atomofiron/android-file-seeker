package app.atomofiron.searchboxapp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import androidx.exifinterface.media.ExifInterface
import app.atomofiron.searchboxapp.model.CacheConfig


object MediaDelegate {

    fun String.getThumbnail(config: CacheConfig): Drawable? {
        var picture = BitmapFactory.decodeFile(this)
        picture ?: return null

        val exif = ExifInterface(this)
        if (exif.rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.setRotate(exif.rotationDegrees.toFloat())
            val recycle = picture
            picture = Bitmap.createBitmap(picture, 0, 0, picture.width, picture.height, matrix, false)
            recycle.recycle()
        }

        val size = config.thumbnailSize
        if (size <= 0) return null
        val width = if (picture.width < picture.height) size else picture.width * size / picture.height
        val height = if (picture.height < picture.width) size else picture.height * size / picture.width
        val bitmap = ThumbnailUtils.extractThumbnail(picture, width, height)
        return BitmapDrawable(bitmap)
    }
}