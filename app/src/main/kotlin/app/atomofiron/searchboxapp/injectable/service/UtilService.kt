package app.atomofiron.searchboxapp.injectable.service

import android.content.ClipboardManager
import android.content.Intent
import androidx.core.content.FileProvider
import app.atomofiron.common.util.extension.copy
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.android.Intents.useAs
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import java.io.File

class UtilService(
    appStore: AppStore,
    private val clipboardManager: ClipboardManager,
) {
    private val context = appStore.context
    private val resources by appStore.resourcesProperty

    fun copyToClipboard(item: Node) = copyToClipboard(item.name, item.path)

    fun copyToClipboard(label: String, text: String) = clipboardManager.copy(context, label, text, resources)

    fun getUriForFile(file: File) = FileProvider.getUriForFile(context, BuildConfig.AUTHORITY, file)

    fun canUseAs(item: Node) = getUseAs(item) != null

    fun useAs(item: Node) {
        val intent = getUseAs(item) ?: return
        context.startActivity(intent)
    }

    private fun getUseAs(item: Node): Intent? {
        val mimeType = item.content.mimeType
            ?.takeIf { it != NodeContent.AnyType }
            ?: return null
        val uri = getUriForFile(File(item.path))
        return context.useAs(uri, mimeType)
    }
}