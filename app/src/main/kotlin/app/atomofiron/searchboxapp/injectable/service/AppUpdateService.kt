package app.atomofiron.searchboxapp.injectable.service

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.res.Resources
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.materialColor
import app.atomofiron.common.util.property.RoProperty
import app.atomofiron.searchboxapp.MaterialAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.injectable.store.AppUpdateStore
import app.atomofiron.searchboxapp.model.other.AppUpdateState
import app.atomofiron.searchboxapp.model.other.UpdateType
import app.atomofiron.searchboxapp.poop
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
    resources: RoProperty<Resources>,
    private val store: AppUpdateStore,
    private val preferences: PreferenceStore,
) : InstallStateUpdatedListener, ActivityResultCallback<ActivityResult> {

    private val resources by resources
    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(context) }
    private var appUpdateInfo: AppUpdateInfo? = null
    private lateinit var launcher: ActivityResultLauncher<IntentSenderRequest>

    init {
        poop("init registerListener")
        appUpdateManager.registerListener(this)
    }

    fun onActivityCreate(activity: AppCompatActivity) {
        launcher = activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult(), this)
    }

    override fun onStateUpdate(state: InstallState) {
        poop("onStateUpdate ${state.installStatus()}")
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
        poop("onActivityResult ${result.resultCode}")
        if (result.resultCode != Activity.RESULT_OK) {
            store.fallback()
        }
    }

    fun check() {
        poop("check")
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            poop("addOnSuccessListener ${appUpdateInfo.updateAvailability()} code ${appUpdateInfo.availableVersionCode()} installStatus ${appUpdateInfo.installStatus()}")
            this.appUpdateInfo = appUpdateInfo
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> return@addOnSuccessListener
                UpdateAvailability.UNKNOWN -> AppUpdateState.Unknown
                UpdateAvailability.UPDATE_NOT_AVAILABLE -> AppUpdateState.UpToDate
                UpdateAvailability.UPDATE_AVAILABLE -> appUpdateInfo.type()?.let {
                    showNotificationForUpdate(appUpdateInfo.availableVersionCode())
                    AppUpdateState.Available(it)
                } ?: AppUpdateState.Unknown
                else -> return@addOnSuccessListener
            }.let { store.set(it) }
        }.addOnFailureListener {
            poop("addOnFailureListener $it")
            store.set(AppUpdateState.Unknown)
        }
    }

    fun startUpdate(variant: UpdateType.Variant) {
        val appUpdateInfo = appUpdateInfo ?: return poop("startUpdate appUpdateInfo null")
        poop("startUpdate startUpdateFlowForResult...")
        val type = when (variant) {
            UpdateType.Flexible -> AppUpdateType.FLEXIBLE
            UpdateType.Immediate -> AppUpdateType.IMMEDIATE
        }
        val options = AppUpdateOptions.newBuilder(type).build()
        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, launcher, options)
    }

    fun completeUpdate() {
        poop("completeUpdate")
        appUpdateManager.completeUpdate()
    }

    private fun showNotificationForUpdate(versionCode: Int) = context.tryShow {
        poop("showNotificationForUpdate")
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
