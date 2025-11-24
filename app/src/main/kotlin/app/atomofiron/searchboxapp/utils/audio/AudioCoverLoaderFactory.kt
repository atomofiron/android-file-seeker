package app.atomofiron.searchboxapp.utils.audio

import android.graphics.Bitmap
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory

object AudioCoverLoaderFactory : ModelLoaderFactory<AudioCover, Bitmap> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<AudioCover, Bitmap> = AudioCoverLoader

    override fun teardown() = Unit
}
