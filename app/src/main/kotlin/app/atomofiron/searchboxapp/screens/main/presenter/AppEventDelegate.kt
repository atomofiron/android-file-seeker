package app.atomofiron.searchboxapp.screens.main.presenter

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.injectable.channel.MainChannel
import app.atomofiron.searchboxapp.injectable.service.AppUpdateService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.main.MainRouter
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.Intents
import kotlinx.coroutines.*

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
    preferenceStore: PreferenceStore,
    private val mainChannel: MainChannel,
    private val updateService: AppUpdateService,
) : AppEventDelegateApi {

    private val notificationManager = NotificationManagerCompat.from(appStore.context)
    private var currentTheme: AppTheme? = null

    init {
        preferenceStore.appTheme.collect(scope, ::onThemeApplied)
    }

    override fun onActivityCreate(activity: AppCompatActivity) {
        appStore.onActivityCreate(activity)
        appStore.onResourcesChange(activity.resources)
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
                notificationManager.cancel(Const.NOTIFICATION_ID_UPDATE)
                router.showSettings()
            }
        }
    }

    override fun onMaximize() = mainChannel.maximized.invoke(scope)

    override fun onActivityDestroy() = appStore.onActivityDestroy()

    override fun onActivityFinish() = updateService.completeUpdate()

    private fun onThemeApplied(theme: AppTheme) {
        if (currentTheme != null && theme != currentTheme) {
            router.recreateActivity()
        }
        currentTheme = theme
    }
}