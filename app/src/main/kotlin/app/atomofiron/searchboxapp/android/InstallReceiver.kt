package app.atomofiron.searchboxapp.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.EXTRA_STATUS
import android.content.pm.PackageInstaller.EXTRA_STATUS_MESSAGE
import android.content.pm.PackageInstaller.STATUS_FAILURE
import android.content.pm.PackageInstaller.STATUS_PENDING_USER_ACTION
import android.content.pm.PackageInstaller.STATUS_SUCCESS
import androidx.core.content.IntentCompat
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.injectable.interactor.DialogInteractor
import javax.inject.Inject

class InstallReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dialogs: DialogInteractor

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra(EXTRA_STATUS, STATUS_FAILURE)) {
            STATUS_SUCCESS -> if (intent.action == Intents.ACTION_INSTALL_UPDATE) {
                context.showAppUpdatedNotification()
            }
            STATUS_PENDING_USER_ACTION -> {
                val activityIntent = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
                context.startActivity(activityIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            else -> {
                DaggerInjector.appComponent.inject(this)
                val message = intent.getStringExtra(EXTRA_STATUS_MESSAGE)
                    ?: context.getString(R.string.unknown_error)
                dialogs.showError(message)
            }
        }
    }
}