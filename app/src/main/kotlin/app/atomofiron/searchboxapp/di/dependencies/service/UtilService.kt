package app.atomofiron.searchboxapp.di.dependencies.service

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import app.atomofiron.common.util.extension.copy
import app.atomofiron.searchboxapp.android.Intents.useAs
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.utils.getUriForFile
import java.io.File

class UtilService(
    private val context: Context,
    resources: AppResources,
    private val clipboardManager: ClipboardManager,
) {
    private val resources by resources

    fun copyToClipboard(item: Node, withAlert: Boolean = false) = copyToClipboard(item.name, item.path, withAlert)

    fun copyToClipboard(label: String, text: String, withAlert: Boolean = false) = clipboardManager.copy(context, label, text, resources, withAlert)

    fun canUseAs(item: Node) = getUseAs(item) != null

    fun useAs(item: Node) {
        val intent = getUseAs(item) ?: return
        context.startActivity(intent)
    }

    private fun getUseAs(item: Node): Intent? {
        val mimeType = item.content.mimeType
            ?.takeIf { it != NodeContent.AnyType }
            ?: return null
        val uri = context.getUriForFile(File(item.path))
        return context.useAs(uri, mimeType)
    }
}