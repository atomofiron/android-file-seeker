package app.atomofiron.searchboxapp.injectable.service

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.store.AppUpdateStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.other.AppUpdateState
import app.atomofiron.searchboxapp.model.other.UpdateType
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
    private val store: AppUpdateStore,
    private val preferences: PreferenceStore,
    private val preferenceChannel: PreferenceChannel,
) : InstallStateUpdatedListener, ActivityResultCallback<ActivityResult> {

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(context) }
    private var appUpdateInfo: AppUpdateInfo? = null
    private lateinit var launcher: ActivityResultLauncher<IntentSenderRequest>
    private var knownActualCode: Int
        get() = preferences.appUpdateCode.value
        set(value) { preferences { setAppUpdateCode(value) } }

    init {
        appUpdateManager.registerListener(this)
    }

    fun onActivityCreate(activity: AppCompatActivity) {
        launcher = activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult(), this)
    }

    override fun onStateUpdate(state: InstallState) {
        val downloadingProgress = state.totalBytesToDownload()
            .takeIf { it > 0 }
            ?.toFloat()
            ?.let { state.bytesDownloaded() / it }
        onStateUpdate(state.installStatus(), downloadingProgress)
    }

    private fun onStateUpdate(status: Int, downloadingProgress: Float?) {
        when (status) {
            InstallStatus.UNKNOWN -> AppUpdateState.Unknown
            // PENDING before DOWNLOADING
            InstallStatus.PENDING -> AppUpdateState.Downloading(null)
            InstallStatus.DOWNLOADING -> AppUpdateState.Downloading(downloadingProgress)
            InstallStatus.FAILED,
            InstallStatus.CANCELED -> return onFailure()
            InstallStatus.DOWNLOADED -> AppUpdateState.Completable
            InstallStatus.INSTALLING -> AppUpdateState.Installing
            InstallStatus.INSTALLED -> AppUpdateState.UpToDate
            else -> return
        }.let { store.set(it) }
    }

    override fun onActivityResult(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            onFailure() // needed in case when cancel without staring update
        }
    }

    fun check(userAction: Boolean = false) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            this.appUpdateInfo = appUpdateInfo
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UNKNOWN -> AppUpdateState.Unknown
                UpdateAvailability.UPDATE_NOT_AVAILABLE -> when {
                    knownActualCode > BuildConfig.VERSION_CODE -> AppUpdateState.Unknown // something went wrong
                    else -> AppUpdateState.UpToDate.also {
                        if (userAction) preferenceChannel.notifyUpdateStatus(context.getString(R.string.is_up_to_date))
                    }
                }
                UpdateAvailability.UPDATE_AVAILABLE -> appUpdateInfo.type()?.let {
                    val versionCode = appUpdateInfo.availableVersionCode()
                    knownActualCode = versionCode
                    AppUpdateState.Available(it, versionCode)
                }
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    onStateUpdate(appUpdateInfo.installStatus(), downloadingProgress = null)
                    return@addOnSuccessListener
                }
                else -> return@addOnSuccessListener
            }.let { store.set(it ?: AppUpdateState.Unknown) }
        }.addOnFailureListener {
            store.set(AppUpdateState.Unknown)
        }
    }

    fun startUpdate(variant: UpdateType.Variant) {
        val appUpdateInfo = appUpdateInfo ?: return
        this.appUpdateInfo = null // spent out
        val type = when (variant) {
            UpdateType.Flexible -> AppUpdateType.FLEXIBLE
            UpdateType.Immediate -> AppUpdateType.IMMEDIATE
        }
        val options = AppUpdateOptions.newBuilder(type).build()
        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, launcher, options)
    }

    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    private fun onFailure() {
        store.fallback()
        check() // to get the new instance of AppUpdateInfo, otherwise startUpdateFlowForResult() stops working
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
