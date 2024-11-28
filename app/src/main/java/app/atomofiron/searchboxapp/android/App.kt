package app.atomofiron.searchboxapp.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Configuration
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.getMarketIntent
import app.atomofiron.searchboxapp.utils.immutable
import com.google.android.material.R as MaterialR
import com.google.android.material.color.DynamicColors
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import javax.inject.Inject

class App : Application(), Configuration.Provider {

    override val workManagerConfiguration = Configuration.Builder().build()

    @Inject
    lateinit var initialDelegate: InitialDelegate

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        DaggerInjector.init(this)
        DaggerInjector.appComponent.inject(this)

        initialDelegate.applyTheme()

        checkForUpdate(this)
    }

    private fun checkForUpdate(context: Context) {
        val appUpdateManager = AppUpdateManagerFactory.create(context)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            val availability = appUpdateInfo.updateAvailability()
            val isAvailable = availability == UpdateAvailability.UPDATE_AVAILABLE
            when {
                isAvailable -> showNotificationForUpdate()
                !BuildConfig.DEBUG -> {
                    val throwable = Throwable("${Const.ERROR_UPDATE_AVAILABILITY} Availability: $availability")
                    // reportError(Const.ERROR_UPDATE_AVAILABILITY, throwable)
                }
            }
        }
        appUpdateInfoTask.addOnFailureListener {
            if (!BuildConfig.DEBUG) {
                // reportError(Const.ERROR_CHECK_UPDATE, Throwable(it.message))
            }
        }
    }

    private fun showNotificationForUpdate() {
        val notificationManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = notificationManager.getNotificationChannel(Const.NOTIFICATION_CHANNEL_UPDATE_ID)
            if (channel == null) {
                channel = NotificationChannel(
                        Const.NOTIFICATION_CHANNEL_UPDATE_ID,
                        getString(R.string.channel_name_updates),
                        NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }
        }
        val flag = PendingIntent.FLAG_UPDATE_CURRENT.immutable()
        val notificationIntent = PendingIntent.getActivity(this, Const.REQUEST_CODE_MARKET_UPDATE, getMarketIntent(), flag)
        val actionIntent = PendingIntent.getActivity(this, Const.REQUEST_CODE_MARKET_UPDATE, getMarketIntent(), flag)
        val notification = NotificationCompat.Builder(this, Const.NOTIFICATION_CHANNEL_UPDATE_ID)
                .setTicker(getString(R.string.update_available))
                .setContentTitle(getString(R.string.update_available))
                .setSmallIcon(R.drawable.ic_notification_update)
                .setContentIntent(notificationIntent)
                .addAction(0, getString(R.string.get_update), actionIntent)
                .setColor(findColorByAttr(MaterialR.attr.colorPrimary))
                .build()

        // todo check permission
        notificationManager.notify(Const.NOTIFICATION_ID_UPDATE, notification)
    }
}