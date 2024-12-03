package app.atomofiron.searchboxapp.injectable.service

import android.content.ClipData
import android.content.ClipboardManager
import androidx.work.WorkManager
import app.atomofiron.searchboxapp.model.explorer.Node
import java.util.*

class ResultService(
    private val workManager: WorkManager,
    private val clipboardManager: ClipboardManager,
) {

    fun stop(uuid: UUID) = workManager.cancelWorkById(uuid)

    fun copyToClipboard(item: Node) {
        val clip = ClipData.newPlainText(item.name, item.path)
        clipboardManager.setPrimaryClip(clip)
    }
}