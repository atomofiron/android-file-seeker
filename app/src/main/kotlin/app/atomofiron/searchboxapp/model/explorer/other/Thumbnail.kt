package app.atomofiron.searchboxapp.model.explorer.other

import android.graphics.Bitmap as AndroidBitmap
import android.graphics.drawable.Drawable as AndroidDrawable

sealed interface Thumbnail {
    @JvmInline
    value class Bitmap(val value: AndroidBitmap): Thumbnail
    @JvmInline
    value class FilePath(val value: String): Thumbnail
    @JvmInline
    value class Drawable(val value: AndroidDrawable): Thumbnail

    val path: String? get() = (this as? FilePath)?.value

    val drawable: AndroidDrawable? get() = (this as? Drawable)?.value
}

val AndroidBitmap.forNode: Thumbnail get() = Thumbnail.Bitmap(this)

val AndroidDrawable.forNode: Thumbnail get() = Thumbnail.Drawable(this)
