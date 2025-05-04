package app.atomofiron.searchboxapp.screens.main.presenter

import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.searchboxapp.android.dismissUpdateNotification
import app.atomofiron.searchboxapp.android.showUpdateNotification
import app.atomofiron.searchboxapp.injectable.channel.MainChannel
import app.atomofiron.searchboxapp.injectable.service.AppUpdateService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.AppStoreConsumer
import app.atomofiron.searchboxapp.injectable.store.AppUpdateStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.other.AppUpdateState
import app.atomofiron.searchboxapp.model.other.UpdateNotification
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.main.MainRouter
import kotlinx.coroutines.CoroutineScope

interface AppEventDelegateApi {
    fun onActivityCreate(activity: AppCompatActivity)
    fun onActivityDestroy()
    fun onIntent(intent: Intent)
    fun onMaximize()
    fun onActivityFinish()
}

class AppEventDelegate(
    private val scope: CoroutineScope,
    private val router: MainRouter,
    private val appStore: AppStore,
    private val appStoreConsumer: AppStoreConsumer,
    private val preferences: PreferenceStore,
    updateStore: AppUpdateStore,
    private val mainChannel: MainChannel,
    private val updateService: AppUpdateService,
) : AppStore by appStore, AppEventDelegateApi {

    private var currentTheme: AppTheme? = null

    init {
        preferences.appTheme.collect(scope, ::onThemeApplied)
        updateStore.state.collect(scope, ::onUpdateState)
    }

    override fun onActivityCreate(activity: AppCompatActivity) {
        appStoreConsumer.onActivityCreate(activity)
        appStoreConsumer.onResourcesChange(activity.resources)
        updateService.onActivityCreate(activity)
    }

    override fun onIntent(intent: Intent) {
        when (intent.action) {
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
}