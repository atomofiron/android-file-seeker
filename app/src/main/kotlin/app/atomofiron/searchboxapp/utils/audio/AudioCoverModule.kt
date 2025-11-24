package app.atomofiron.searchboxapp.utils.audio

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@Suppress("unused") // used
@GlideModule
class AudioCoverModule : AppGlideModule() {

    private val factory = AudioCoverLoaderFactory

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(AudioCover::class.java, Bitmap::class.java, factory)
    }
}
