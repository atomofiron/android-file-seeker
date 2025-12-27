package app.atomofiron.searchboxapp.di.dependencies.service

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.copy
import app.atomofiron.searchboxapp.android.Intents.useAs
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.di.dependencies.store.Strings
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.utils.getUriForFile
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UtilService @Inject constructor(
    private val context: Context,
    resources: AppResources,
    private val clipboardManager: ClipboardManager,
    preference: PreferenceStore,
) : Strings by resources {

    private val resources by resources
    private val asSu by preference.asSu

    fun copyToClipboard(item: Node, withAlert: Boolean = Android.Below.T) = copyToClipboard(item.name, item.path, withAlert)

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
        val uri = context.getUriForFile(File(item.path), asSu)
        return context.useAs(uri, mimeType)
    }
}