package app.atomofiron.searchboxapp.model.explorer.other

import androidx.annotation.DrawableRes
import android.graphics.Bitmap as AndroidBitmap
import android.graphics.drawable.Drawable as AndroidDrawable

sealed interface Thumbnail {

    val ready: Boolean get() = true

    data object Loading: Thumbnail {
        override val ready = false
    }
    data object FilePath : Thumbnail
    @JvmInline
    value class Bitmap(val value: AndroidBitmap) : Thumbnail
    @JvmInline
    value class Drawable(val value: AndroidDrawable) : Thumbnail
    @JvmInline
    value class Res(@DrawableRes val value: Int) : Thumbnail

    val drawable: AndroidDrawable? get() = (this as? Drawable)?.value

    companion object {
        operator fun invoke(value: AndroidBitmap) = Bitmap(value)
        operator fun invoke(value: AndroidDrawable) = Drawable(value)
        operator fun invoke(@DrawableRes value: Int) = Res(value)
    }
}

val AndroidBitmap.forNode: Thumbnail get() = Thumbnail.Bitmap(this)
