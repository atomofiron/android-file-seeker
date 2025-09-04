package app.atomofiron.searchboxapp.di.dependencies.router

import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import app.atomofiron.common.util.ActivityProperty
import app.atomofiron.common.util.extension.unit
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.utils.Const
import java.io.File


class FileSharingDelegateImpl(activityProperty: ActivityProperty) : FileSharingDelegate, FilePickingDelegate {

    private val activity by activityProperty

    override fun openWith(item: Node) {
        activity?.startForFile(Intent.ACTION_VIEW, item)
    }

    override fun shareWith(items: List<Node>) {
        val context = activity ?: return
        if (items.isEmpty()) return
        if (items.size == 1) {
            activity?.startForFile(Intent.ACTION_SEND, items.first())
            return
        }
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
        } ?: NodeContent.AnyType
        val intent = Intent(action)
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)
        intent.setDataAndType(contentUri, type)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val chooser = Intent.createChooser(intent, null)
        startActivity(chooser)
    }

    override fun shareSinglePicked(item: Node) = activity?.run {
        val file = File(item.path)
        val uri = FileProvider.getUriForFile(this, BuildConfig.AUTHORITY, file)
        val data = Intent().apply {
            setDataAndType(uri, contentResolver.getType(uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        setResult(RESULT_OK, data)
        finish()
    }.unit()

    override fun shareMultiplePicked(items: List<Node>) = activity?.run {
        val uris = items.map {
            FileProvider.getUriForFile(this, BuildConfig.AUTHORITY, File(it.path))
        }
        val clip = ClipData.newUri(contentResolver, Const.SELECTED, uris.first())
        for (i in 1..<uris.size) {
            clip.addItem(ClipData.Item(uris[i]))
        }
        val data = Intent()
        data.clipData = clip
        data.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        setResult(RESULT_OK, data)
        finish()
    }.unit()
}

interface FileSharingDelegate {
    fun openWith(item: Node)
    fun shareWith(items: List<Node>)
}

interface FilePickingDelegate {
    fun shareSinglePicked(item: Node)
    fun shareMultiplePicked(items: List<Node>)
}