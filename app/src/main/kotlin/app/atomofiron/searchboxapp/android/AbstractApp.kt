package app.atomofiron.searchboxapp.android

import android.app.Application
import app.atomofiron.common.util.extension.debugContext
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.di.dependencies.delegate.InitialDelegate
import app.atomofiron.searchboxapp.di.dependencies.delegate.StorageDelegate
import app.atomofiron.searchboxapp.di.dependencies.service.AppUpdateService
import app.atomofiron.searchboxapp.model.AppSource
import com.google.android.material.color.DynamicColors
import java.lang.ref.WeakReference
import javax.inject.Inject

abstract class AbstractApp : Application() {

    @Inject
    lateinit var initialDelegate: InitialDelegate
    @Inject
    lateinit var storageDelegate: StorageDelegate

    @Inject
    lateinit var updateService: AppUpdateService

    protected abstract val appSource: AppSource
    protected abstract val updateServiceFactory: AppUpdateService.Factory

    override fun onCreate() {
        super.onCreate()

        debugContext = WeakReference(this)

        DynamicColors.applyToActivitiesIfAvailable(this)

        DaggerInjector.init(this, appSource, updateServiceFactory)
        DaggerInjector.appComponent.inject(this)

        initialDelegate.applyTheme()
        if (!BuildConfig.DEBUG) updateService.check()
    }
}