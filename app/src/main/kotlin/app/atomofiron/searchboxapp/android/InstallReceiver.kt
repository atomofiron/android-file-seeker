package app.atomofiron.searchboxapp.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.EXTRA_PACKAGE_NAME
import android.content.pm.PackageInstaller.EXTRA_STATUS
import android.content.pm.PackageInstaller.EXTRA_STATUS_MESSAGE
import android.content.pm.PackageInstaller.STATUS_FAILURE
import android.content.pm.PackageInstaller.STATUS_PENDING_USER_ACTION
import android.content.pm.PackageInstaller.STATUS_SUCCESS
import androidx.core.content.IntentCompat
import app.atomofiron.common.util.DialogMaker
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.utils.launch
import javax.inject.Inject

class InstallReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dialogs: DialogMaker

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra(EXTRA_STATUS, STATUS_FAILURE)) {
            STATUS_SUCCESS -> when (intent.action) {
                Intents.ACTION_INSTALL_UPDATE -> context.showAppUpdatedNotification()
                Intents.ACTION_INSTALL_APP -> context.offerLaunch(intent)
            }
            STATUS_PENDING_USER_ACTION -> {
                val activityIntent = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
                context.startActivity(activityIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            else -> context.showError(intent)
        }
    }

    private fun Context.showError(intent: Intent) {
        inject()
        val message = intent.getStringExtra(EXTRA_STATUS_MESSAGE) ?: getString(R.string.unknown_error)
        dialogs.showError(message)
    }

    private fun Context.offerLaunch(intent: Intent) {
        inject()
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        dialogs.show(
            cancelable = false,
            title = UniText(R.string.install_succeeded),
            negative = DialogMaker.Cancel,
            positive = UniText(R.string.launch),
            onPositiveClick = { launch(packageName.toString()) },
        )
    }

    private fun inject() = DaggerInjector.appComponent.inject(this)
}