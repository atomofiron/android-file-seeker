package app.atomofiron.searchboxapp.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import app.atomofiron.common.util.flow.set
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.android.ScreenshotService.Companion.initScreenshotService
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.channel.CommonChannel
import app.atomofiron.searchboxapp.di.dependencies.delegate.InitialDelegate
import app.atomofiron.searchboxapp.di.dependencies.delegate.StorageDelegate
import app.atomofiron.searchboxapp.di.dependencies.service.AppUpdateService
import app.atomofiron.searchboxapp.model.AppSource
import app.atomofiron.searchboxapp.model.other.AppState
import com.google.android.material.color.DynamicColors
import javax.inject.Inject

abstract class AbstractApp : Application(), LifecycleEventObserver {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var appContext: Context // this is the Application instance
            private set
    }

    @Inject
    lateinit var initialDelegate: InitialDelegate
    @Inject
    lateinit var storageDelegate: StorageDelegate
    @Inject
    lateinit var updateService: AppUpdateService
    @Inject
    lateinit var screenshotDeps: ScreenshotServiceDependencies
    @Inject
    lateinit var commonChannel: CommonChannel
    @Inject
    lateinit var scope: AppScope

    protected abstract val appSource: AppSource
    protected abstract val updateServiceFactory: AppUpdateService.Factory

    override fun onCreate() {
        super.onCreate()

        appContext = this

        DynamicColors.applyToActivitiesIfAvailable(this)

        DaggerInjector.init(this, appSource, updateServiceFactory)
        DaggerInjector.appComponent.inject(this)

        initialDelegate.applyTheme()
        if (!BuildConfig.DEBUG) updateService.check()
        initScreenshotService(screenshotDeps)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val prev = commonChannel.appState.value
        commonChannel.appState[scope] = when (source.lifecycle.currentState) {
            Lifecycle.State.RESUMED -> AppState.Foreground
            Lifecycle.State.STARTED -> AppState.Started(prev = prev)
            else -> AppState.Unknown
        }
    }
}