package app.atomofiron.searchboxapp.model.explorer.other

import android.graphics.Bitmap as AndroidBitmap
import android.graphics.drawable.Drawable as AndroidDrawable

sealed interface Thumbnail {
    @JvmInline
    value class Bitmap(val value: AndroidBitmap): Thumbnail
    @JvmInline
    value class Drawable(val value: AndroidDrawable): Thumbnail

    val bitmap: AndroidBitmap? get() = (this as? Bitmap)?.value

    val drawable: AndroidDrawable? get() = (this as? Drawable)?.value
}

val AndroidBitmap.forNode: Thumbnail get() = Thumbnail.Bitmap(this)

val AndroidDrawable.forNode: Thumbnail get() = Thumbnail.Drawable(this)
