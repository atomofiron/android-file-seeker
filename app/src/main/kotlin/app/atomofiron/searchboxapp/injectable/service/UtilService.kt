package app.atomofiron.searchboxapp.injectable.service

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.model.explorer.Node

class UtilService(
    appStore: AppStore,
    private val clipboardManager: ClipboardManager,
) {
    private val context = appStore.context
    private val resources by appStore.resourcesProperty

    fun copyToClipboard(item: Node) = copyToClipboard(item.name, item.path)

    fun copyToClipboard(label: String, text: String) {
        val clip = ClipData.newPlainText(label, text)
        val toast = try {
            clipboardManager.setPrimaryClip(clip)
            resources.getString(R.string.copied)
        } catch (e: Exception) {
            e.toString()
        }
        Toast.makeText(context, toast, Toast.LENGTH_LONG).show()
    }
}