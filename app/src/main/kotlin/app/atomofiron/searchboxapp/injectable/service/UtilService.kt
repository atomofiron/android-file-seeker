package app.atomofiron.searchboxapp.injectable.service

import android.content.ClipboardManager
import app.atomofiron.common.util.extension.copy
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.model.explorer.Node

class UtilService(
    appStore: AppStore,
    private val clipboardManager: ClipboardManager,
) {
    private val context = appStore.context
    private val resources by appStore.resourcesProperty

    fun copyToClipboard(item: Node) = copyToClipboard(item.name, item.path)

    fun copyToClipboard(label: String, text: String) = clipboardManager.copy(context, label, text, resources)
}