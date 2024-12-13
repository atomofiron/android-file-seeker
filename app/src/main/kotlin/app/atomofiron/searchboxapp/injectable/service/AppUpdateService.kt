package app.atomofiron.searchboxapp.injectable.service

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.res.Resources
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.materialColor
import app.atomofiron.common.util.property.MultiLazy
import app.atomofiron.common.util.property.RoProperty
import app.atomofiron.searchboxapp.MaterialAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.injectable.store.AppUpdateStore
import app.atomofiron.searchboxapp.model.other.AppUpdateState
import app.atomofiron.searchboxapp.model.other.UpdateType
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.immutable
import app.atomofiron.searchboxapp.utils.tryShow
import app.atomofiron.searchboxapp.utils.updateIntent
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class AppUpdateService(
    private val context: Context,
    activity: RoProperty<AppCompatActivity?>,
    resources: RoProperty<Resources>,
    private val store: AppUpdateStore,
    private val preferences: PreferenceStore,
) : InstallStateUpdatedListener, ActivityResultCallback<ActivityResult> {

    private val resources by resources
    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(context) }
    private var appUpdateInfo: AppUpdateInfo? = null
    private val launcher by MultiLazy {
        activity.value?.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult(), this)
    }

    init {
        appUpdateManager.registerListener(this)
    }

    override fun onStateUpdate(state: InstallState) {
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                val bytes = state.bytesDownloaded()
                val total = state.totalBytesToDownload()
                AppUpdateState.Downloading(bytes.toFloat() / total)
            }
            InstallStatus.FAILED,
            InstallStatus.CANCELED -> return store.fallback()
            InstallStatus.UNKNOWN -> AppUpdateState.Unknown
            InstallStatus.INSTALLED /*?*/ -> AppUpdateState.UpToDate
            InstallStatus.DOWNLOADED -> AppUpdateState.Completable
            InstallStatus.INSTALLING /*?*/ -> AppUpdateState.Installing
            InstallStatus.PENDING /*?*/ ->  AppUpdateState.Completable
            else -> return
        }.let { store.set(it) }
    }

    override fun onActivityResult(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            store.fallback()
        }
    }

    fun check() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            this.appUpdateInfo = appUpdateInfo
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> return@addOnSuccessListener
                UpdateAvailability.UNKNOWN -> AppUpdateState.Unknown
                UpdateAvailability.UPDATE_NOT_AVAILABLE -> AppUpdateState.UpToDate
                UpdateAvailability.UPDATE_AVAILABLE -> appUpdateInfo.type()?.let {
                    showNotificationForUpdate()
                    AppUpdateState.Available(it)
                } ?: AppUpdateState.Unknown
                else -> return@addOnSuccessListener
            }.let { store.set(it) }
        }.addOnFailureListener {
            store.set(AppUpdateState.Unknown)
        }
    }

    fun startUpdate(immediate: Boolean) {
        val launcher = launcher ?: return
        val appUpdateInfo = appUpdateInfo ?: return
        val options = AppUpdateOptions.newBuilder(if (immediate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE).build()
        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, launcher, options)
    }

    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    private fun showNotificationForUpdate() = context.tryShow {
        val manager = NotificationManagerCompat.from(context)
        if (Android.O) {
            var channel = manager.getNotificationChannel(Const.NOTIFICATION_CHANNEL_UPDATE_ID)
            if (channel == null) {
                channel = NotificationChannel(
                    Const.NOTIFICATION_CHANNEL_UPDATE_ID,
                    resources.getString(R.string.channel_name_updates),
                    NotificationManager.IMPORTANCE_HIGH,
                )
            }
            val lastCode = preferences.lastUpdateNotificationCode.value
            preferences { setLastUpdateNotificationCode(versionCode) }
            channel.importance = if (lastCode < versionCode) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_MIN
            manager.createNotificationChannel(channel)
        }
        val flag = PendingIntent.FLAG_UPDATE_CURRENT.immutable()
        Const.NOTIFICATION_ID_UPDATE to NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_UPDATE_ID)
            .setTicker(resources.getString(R.string.update_available))
            .setContentTitle(resources.getString(R.string.update_available))
            .setSmallIcon(R.drawable.ic_notification_update)
            .setContentIntent(PendingIntent.getActivity(context, Const.REQUEST_CODE_UPDATE, context.updateIntent(), flag))
            .setColor(context.materialColor(MaterialAttr.colorPrimary))
            .build()
    }
}

private fun AppUpdateInfo.type(): UpdateType? {
    val immediate = isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
    val flexible = isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
    return when {
        immediate && flexible -> UpdateType.All
        immediate -> UpdateType.Immediate
        flexible -> UpdateType.Flexible
        else -> null
    }
}
