package app.atomofiron.searchboxapp.utils.audio

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.util.Size
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.logE
import app.atomofiron.fileseeker.BuildConfig
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.File

class AudioCoverFetcher(
    private val audio: AudioCover,
    private val size: Size,
) : DataFetcher<Bitmap> {

    private var isCancelled = false

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        if (isCancelled) {
            return
        }
        try {
            val bitmap = audio.createThumbnail()
            when (bitmap) {
                null -> callback.onLoadFailed(AudioCoverException)
                else -> callback.onDataReady(bitmap)
            }
        } catch (e: Exception) {
            e.print(audio.path)
            callback.onLoadFailed(e)
        }
    }

    override fun cleanup() = Unit

    override fun cancel() {
        isCancelled = true
    }

    override fun getDataClass(): Class<Bitmap> = Bitmap::class.java

    override fun getDataSource(): DataSource = DataSource.LOCAL

    private fun AudioCover.createThumbnail() = when {
        Android.Q -> ThumbnailUtils.createAudioThumbnail(File(path), size, null)
        else -> {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            retriever.embeddedPicture
                ?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                .also { retriever.release() }
        }
    }

    private fun Exception.print(path: String) {
        val message = when {
            BuildConfig.DEBUG -> "$path $this"
            else -> toString()
        }
        logE(message)
    }
}
