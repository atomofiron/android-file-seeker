package app.atomofiron.searchboxapp.utils.audio

import android.graphics.Bitmap
import android.util.Size
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey

object AudioCoverLoader : ModelLoader<AudioCover, Bitmap> {

    override fun buildLoadData(
        model: AudioCover,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<Bitmap> {
        val fetcher = AudioCoverFetcher(model, Size(width, height))
        return ModelLoader.LoadData(ObjectKey(model.path), fetcher)
    }

    override fun handles(model: AudioCover): Boolean = true
}
