package app.atomofiron.searchboxapp.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.EXTRA_STATUS
import android.content.pm.PackageInstaller.EXTRA_STATUS_MESSAGE
import android.content.pm.PackageInstaller.STATUS_FAILURE
import android.content.pm.PackageInstaller.STATUS_PENDING_USER_ACTION
import android.content.pm.PackageInstaller.STATUS_SUCCESS
import android.widget.Toast
import androidx.core.content.IntentCompat

class InstallReceiver : BroadcastReceiver() {
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
                val message = intent.getStringExtra(EXTRA_STATUS_MESSAGE)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}