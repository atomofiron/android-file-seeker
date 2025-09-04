package app.atomofiron.searchboxapp.screens.main.presenter

import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.dialog.DialogConfig
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.searchboxapp.android.dismissUpdateNotification
import app.atomofiron.searchboxapp.android.showUpdateNotification
import app.atomofiron.searchboxapp.di.dependencies.channel.ApkChannel
import app.atomofiron.searchboxapp.di.dependencies.channel.MainChannel
import app.atomofiron.searchboxapp.di.dependencies.service.AppUpdateService
import app.atomofiron.searchboxapp.di.dependencies.store.AppStoreConsumer
import app.atomofiron.searchboxapp.di.dependencies.store.AppUpdateStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.other.AppUpdateState
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.model.other.UpdateNotification
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.common.delegates.FileOperationsDelegate
import app.atomofiron.searchboxapp.screens.main.MainRouter
import app.atomofiron.searchboxapp.utils.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

interface AppEventDelegateApi {
    fun onActivityCreate(activity: AppCompatActivity)
    fun onActivityDestroy()
    fun onIntent(intent: Intent)
    fun onMaximize()
    fun onActivityFinish()
}

class AppEventDelegate(
    private val context: Context,
    private val scope: CoroutineScope,
    private val router: MainRouter,
    private val appStoreConsumer: AppStoreConsumer,
    private val operations: FileOperationsDelegate,
    private val dialogs: DialogDelegate,
    private val preferences: PreferenceStore,
    updateStore: AppUpdateStore,
    private val mainChannel: MainChannel,
    apkChannel: ApkChannel,
    private val updateService: AppUpdateService,
) : AppEventDelegateApi {

    private var currentTheme: AppTheme? = null
    private val activity get() = router.activity

    init {
        preferences.appTheme.collect(scope, ::onThemeApplied)
        updateStore.state.collect(scope, ::onUpdateState)
        apkChannel.errorMessage.collectWhenResumed(scope) { dialogs.showError(UniText(it)) }
        apkChannel.offerPackageName.collectWhenResumed(scope) { offerLaunch(it) }
    }

    override fun onActivityCreate(activity: AppCompatActivity) {
        appStoreConsumer.onActivityCreate(activity)
        appStoreConsumer.onResourcesChange(activity.resources)
        updateService.onActivityCreate(activity)
    }

    override fun onIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> intent.data?.viewFile()
            Intent.ACTION_SEND -> {
                val uri = when {
                    Android.T -> intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    else -> intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri?
                }
                // todo alerts
                uri ?: return
                mainChannel.fileToReceive[scope] = uri
            }
            Intents.ACTION_UPDATE -> {
                context.dismissUpdateNotification()
                router.showSettings()
            }
        }
    }

    override fun onMaximize() = mainChannel.maximized.invoke(scope)

    override fun onActivityDestroy() = appStoreConsumer.onActivityDestroy()

    override fun onActivityFinish() = updateService.completeUpdate()

    private fun onThemeApplied(theme: AppTheme) {
        val activityNight = activity
            ?.resources
            ?.configuration
            ?.run { (uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES }
        val systemNight = activity
            ?.application
            ?.resources
            ?.configuration
            ?.run { (uiMode and UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES }
        when (true) {
            (currentTheme == null),
            (activityNight == null),
            (systemNight == null) -> Unit
            (theme.system && activityNight && systemNight && theme.deepBlack != currentTheme?.deepBlack),
            (theme.onlyDark && activityNight && theme.deepBlack != currentTheme?.deepBlack) -> router.recreateActivity()
            else -> Unit
        }
        currentTheme = theme
    }

    private fun onUpdateState(state: AppUpdateState) {
        when (state) {
            is AppUpdateState.Available -> {
                val shownCode = preferences.shownNotificationUpdateCode.value
                if (state.code <= shownCode) return
                val shown = context.showUpdateNotification(UpdateNotification.Available)
                if (shown) preferences { setShownNotificationUpdateCode(state.code) }
            }
            is AppUpdateState.Completable -> context.showUpdateNotification(UpdateNotification.Install)
            else -> context.dismissUpdateNotification()
        }
    }

    private fun Uri.viewFile() = operations.askForApks(NodeRef(path = toString()), context.contentResolver)

    private fun offerLaunch(packageName: String) {
        dialogs show DialogConfig(
            cancelable = true,
            title = UniText(R.string.install_succeeded),
            negative = DialogDelegate.Cancel,
            positive = UniText(R.string.launch),
            onPositiveClick = {
                if (!context.launch(packageName.toString())) {
                    dialogs.showError()
                }
            },
        )
    }

    private fun <T> Flow<T>.collectWhenResumed(scope: CoroutineScope, collector: FlowCollector<T>) {
        collect(scope) {
            if (activity?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                collector.emit(it)
            }
        }
    }
}