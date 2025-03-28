package app.atomofiron.searchboxapp.injectable.service

import android.content.ClipData
import android.content.ClipboardManager
import app.atomofiron.searchboxapp.model.explorer.Node

class UtilService(private val clipboardManager: ClipboardManager) {
    fun copyToClipboard(item: Node) {
        val clip = ClipData.newPlainText(item.name, item.path)
        clipboardManager.setPrimaryClip(clip)
    }
}