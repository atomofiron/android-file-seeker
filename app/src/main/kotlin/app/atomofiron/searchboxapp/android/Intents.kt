package app.atomofiron.searchboxapp.android

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import app.atomofiron.common.util.AndroidSdk
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.screens.main.MainActivity
import app.atomofiron.searchboxapp.utils.Const
import androidx.core.net.toUri
import app.atomofiron.common.util.extension.put
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.utils.getUriForFile
import java.io.File

object Intents {
    const val ACTION_UPDATE = "ACTION_UPDATE"
    const val ACTION_INSTALL_UPDATE = "ACTION_INSTALL_UPDATE"
    const val ACTION_INSTALL_APP = "ACTION_INSTALL_APP"

    private const val PACKAGE_SCHEME = "package:"
    private const val MAX_REQUEST_CODE = 65536

    //val telegramLink get() = Intent(Intent.ACTION_VIEW, Uri.parse(Const.TELEGRAM_LINK))
    val github = Intent(Intent.ACTION_VIEW, Const.GITHUB_URL.toUri())
    val forPda = Intent(Intent.ACTION_VIEW, Const.FORPDA_URL.toUri())
    @SuppressLint("InlinedApi")
    val locales = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
        data = Uri.fromParts(Const.SCHEME_PACKAGE, BuildConfig.PACKAGE_NAME, null)
    }

    fun mainActivity(context: Context, action: String? = null) = Intent(context, MainActivity::class.java).setAction(action)

    fun appDetails(context: Context) = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .addCategory(Intent.CATEGORY_DEFAULT)
        .setData(Uri.fromParts(Const.SCHEME_PACKAGE, context.packageName, null))
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun send(uri: Uri) = Intent(Intent.ACTION_SEND)
        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        .setType(Const.MIME_TYPE_ANY)
        .putExtra(Intent.EXTRA_STREAM, uri)

    fun deleteScreenshot(context: Context, ref: NodeRef) = Intent(context, DeleteScreenshotReceiver::class.java).put(ref)

    val settingsIntent: Intent
        get() {
            val packageUri = (PACKAGE_SCHEME + BuildConfig.PACKAGE_NAME).toUri()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            return intent
        }

    val storagePermissionIntent: Intent
        @RequiresApi(AndroidSdk.R)
        get() {
            return Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                (PACKAGE_SCHEME + BuildConfig.PACKAGE_NAME).toUri()
            )
        }

    fun updating(context: Context) = mainActivity(context, ACTION_UPDATE)

    fun installing(context: Context) = Intent(context, InstallReceiver::class.java)

    fun Context.useAs(uri: Uri, mimeType: String?): Intent? {
        val intent = Intent(Intent.ACTION_ATTACH_DATA)
            .setDataAndType(uri, mimeType)
            .putExtra(Const.MIME_TYPE, mimeType)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.resolveActivity(packageManager) ?: return null
        return Intent.createChooser(intent, resources.getString(R.string.use_as))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun openWith(context: Context, ref: NodeRef, content: NodeContent? = null): Intent {
        return chooser(context, Intent.ACTION_VIEW, ref, content)
    }

    fun shareWith(context: Context, ref: NodeRef, content: NodeContent? = null): Intent {
        return chooser(context, Intent.ACTION_SEND, ref, content)
    }

    private fun chooser(context: Context, action: String, ref: NodeRef, content: NodeContent?): Intent {
        val file = File(ref.string)
        val contentUri = context.getUriForFile(file)
        val type = content?.mimeType
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(ref.ext)
            ?: NodeContent.AnyType
        val intent = Intent(action)
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)
        intent.setDataAndType(contentUri, type)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return Intent.createChooser(intent, null)
    }
}