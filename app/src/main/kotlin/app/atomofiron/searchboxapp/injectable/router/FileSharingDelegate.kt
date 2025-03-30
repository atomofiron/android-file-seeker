package app.atomofiron.searchboxapp.injectable.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.poop
import java.io.File


class FileSharingDelegateImpl(activityProperty: WeakProperty<out FragmentActivity>) : FileSharingDelegate {

    private val activity by activityProperty

    override fun openWith(item: Node) {
        activity?.startForFile(Intent.ACTION_VIEW, item)
    }

    override fun shareWith(item: Node) {
        activity?.startForFile(Intent.ACTION_SEND, item)
    }

    override fun shareWith(items: List<Node>) {
        val context = activity ?: return
        if (items.isEmpty()) return
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        var (mimeType, commonMimeType) = items.first().content.run { mimeType to commonMimeType }
        for (item in items) {
            when (mimeType) {
                item.content.mimeType -> Unit
                else -> mimeType = null
            }
            when (commonMimeType) {
                item.content.commonMimeType -> Unit
                else -> commonMimeType = NodeContent.AnyType
            }
        }
        intent.setType(mimeType ?: commonMimeType)
        poop("mimeType ${mimeType ?: commonMimeType}")
        val files = ArrayList<Uri>()
        for (item in items) {
            val file = File(item.path)
            val uri = FileProvider.getUriForFile(context, BuildConfig.AUTHORITY, file)
            files.add(uri)
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val chooser = Intent.createChooser(intent, null)
        context.startActivity(chooser)
    }

    private fun Context.startForFile(action: String, item: Node) {
        val file = File(item.path)
        val contentUri = FileProvider.getUriForFile(this, BuildConfig.AUTHORITY, file)
        val type = item.content.mimeType ?: let {
            val ext = MimeTypeMap.getFileExtensionFromUrl(file.name)
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        } ?: "*/*"
        val intent = Intent(action)
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)
        intent.setDataAndType(contentUri, type)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val chooser = Intent.createChooser(intent, null)
        startActivity(chooser)
    }
}

interface FileSharingDelegate {
    fun openWith(item: Node)
    fun shareWith(item: Node)
    fun shareWith(items: List<Node>)
}