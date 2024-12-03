package app.atomofiron.searchboxapp.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.RequiresApi
import app.atomofiron.common.util.Android
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.screens.main.MainActivity
import app.atomofiron.searchboxapp.utils.Const

object Intents {
    const val ACTION_UPDATE = "ACTION_UPDATE"
    const val ACTION_INSTALL_UPDATE = "ACTION_INSTALL_UPDATE"

    const val REQUEST_UPDATE = 7453
    const val REQUEST_REMINDER = 7454
    const val REQUEST_REMIND_IN_10_MIN = 7455
    const val REQUEST_REMIND_IN_AN_HOUR = 7456
    const val REQUEST_RESTORE_BACKUP = 34576
    const val REQUEST_CODE_UPDATE_APP = 12345

    const val KEY_REMIND_IN_MINUTES = "KEY_REMIND_IN_MINUTES"
    const val KEY_WITH_SOUND = "KEY_WITH_SOUND"

    private const val PACKAGE_SCHEME = "package:"
    private const val MAX_REQUEST_CODE = 65536

    //val telegramLink get() = Intent(Intent.ACTION_VIEW, Uri.parse(Const.TELEGRAM_LINK))

    fun mainActivity(context: Context, action: String? = null) = Intent(context, MainActivity::class.java).setAction(action)

    fun appDetails(context: Context) = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .addCategory(Intent.CATEGORY_DEFAULT)
        .setData(Uri.fromParts(Const.SCHEME_PACKAGE, context.packageName, null))
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun send(uri: Uri) = Intent(Intent.ACTION_SEND)
        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        .setType(Const.MIME_TYPE_ANY)
        .putExtra(Intent.EXTRA_STREAM, uri)

    val settingsIntent: Intent
        get() {
            val packageUri = Uri.parse(PACKAGE_SCHEME + BuildConfig.APPLICATION_ID)
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            return intent
        }

    val storagePermissionIntent: Intent
        @RequiresApi(Android.R)
        get() = Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            Uri.parse(PACKAGE_SCHEME + BuildConfig.APPLICATION_ID)
        )
}