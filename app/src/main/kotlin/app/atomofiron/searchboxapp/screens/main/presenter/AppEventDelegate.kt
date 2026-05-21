package app.atomofiron.searchboxapp.screens.main.presenter

import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.icu.util.Calendar
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.dialog.DialogConfig
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.searchboxapp.android.dismissUpdateNotification
import app.atomofiron.searchboxapp.android.showUpdateNotification
import app.atomofiron.searchboxapp.di.dependencies.channel.ApkChannel
import app.atomofiron.searchboxapp.di.dependencies.channel.CommonChannel
import app.atomofiron.searchboxapp.di.dependencies.delegate.ApkDelegate
import app.atomofiron.searchboxapp.di.dependencies.service.AppUpdateService
import app.atomofiron.searchboxapp.di.dependencies.store.AppUpdateStore
import app.atomofiron.searchboxapp.di.dependencies.store.EasterEggStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.other.AppUpdateState
import app.atomofiron.searchboxapp.model.other.UiMode
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.model.other.UpdateNotification
import app.atomofiron.searchboxapp.model.preference.AppLocale
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.common.delegates.ApkOperationsDelegate
import app.atomofiron.searchboxapp.screens.main.MainRouter
import app.atomofiron.searchboxapp.screens.main.MainScope
import app.atomofiron.searchboxapp.screens.main.di.AppStoreConsumer
import app.atomofiron.searchboxapp.screens.main.model.EasterEgg
import app.atomofiron.searchboxapp.screens.main.util.EasterEggPeriods
import app.atomofiron.searchboxapp.utils.launch
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

interface AppEventDelegateApi {
    fun onActivityCreate(activity: AppCompatActivity)
    fun onActivityDestroy()
    fun onIntent(intent: Intent)
    fun onActivityFinish()
    fun onUserInteraction()
    fun onConfiguration(isDark: Boolean, isBlack: Boolean)
}

@MainScope
class AppEventDelegate @Inject constructor(
    private val context: Context,
    private val scope: CoroutineScope,
    private val router: MainRouter,
    private val appStoreConsumer: AppStoreConsumer,
    private val operations: ApkOperationsDelegate,
    private val apks: ApkDelegate,
    private val dialogs: DialogDelegate,
    private val preferences: PreferenceStore,
    updateStore: AppUpdateStore,
    private val eggStore: EasterEggStore,
    apkChannel: ApkChannel,
    private val commonChannel: CommonChannel,
    private val updateService: AppUpdateService,
) : AppEventDelegateApi, LifecycleEventObserver {

    private var currentTheme: AppTheme? = null
    private val activity get() = router.activity

    init {
        preferences.appTheme[scope] = ::onThemeApplied
        updateStore.state[scope] = ::onUpdateState
        apkChannel.errorMessage[scope] = { dialogs.showError(UniText(it)) }
        apkChannel.offerPackageName[scope] = { offerLaunch(it) }
    }

    override fun onActivityCreate(activity: AppCompatActivity) {
        appStoreConsumer.onActivityCreate(activity)
        appStoreConsumer.onResourcesChange(activity.resources)
        updateService.onActivityCreate(activity)
        if (Android.T) updateLocalePreference()
        activity.lifecycle.addObserver(this)
    }

    override fun onIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> intent.data?.viewFile()
            Intents.ACTION_UPDATE -> {
                context.dismissUpdateNotification()
                router.showSettings()
            }
        }
    }

    override fun onActivityDestroy() = appStoreConsumer.onActivityDestroy()

    override fun onActivityFinish() = updateService.completeUpdate()

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_START) {
            val now = Calendar.getInstance()
            EasterEggPeriods.updateYear(now)
            val value = when (now) {
                in EasterEggPeriods.halloween -> EasterEgg.Halloween
                in EasterEggPeriods.clown -> EasterEgg.Clown
                in EasterEggPeriods.oldYear -> EasterEgg.NewYear
                in EasterEggPeriods.newYear -> EasterEgg.NewYear
                else -> null
            }
            eggStore.set(value)
        }
    }

    override fun onUserInteraction() = commonChannel.userInteraction.invoke(scope)

    override fun onConfiguration(isDark: Boolean, isBlack: Boolean) {
        commonChannel.uiMode[scope] = UiMode(isDark, isBlack)
    }

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

    private fun offerLaunch(packageName: String) = when {
        apks.launchable(packageName) -> dialogs show DialogConfig(
            cancelable = true,
            title = UniText(R.string.install_succeeded),
            negative = DialogDelegate.Cancel,
            positive = UniText(R.string.launch),
            onPositiveClick = {
                if (!context.launch(packageName)) {
                    dialogs.showError()
                }
            },
        )
        else -> dialogs show DialogConfig(
            cancelable = true,
            title = UniText(R.string.install_succeeded),
        )
    }

    private fun updateLocalePreference() {
        val appLocale = AppCompatDelegate.getApplicationLocales().get(0)
            ?.let { AppLocale[it.toLanguageTag()] }
            ?: AppLocale.System
        preferences { setAppLocale(appLocale) }
    }
}