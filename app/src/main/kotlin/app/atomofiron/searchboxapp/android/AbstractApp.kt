package app.atomofiron.searchboxapp.android

import android.app.Application
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import app.atomofiron.searchboxapp.injectable.service.AppUpdateService
import app.atomofiron.searchboxapp.model.AppSource
import com.google.android.material.color.DynamicColors
import javax.inject.Inject

abstract class AbstractApp : Application() {

    @Inject
    lateinit var initialDelegate: InitialDelegate

    @Inject
    lateinit var updateService: AppUpdateService

    protected abstract val appSource: AppSource
    protected abstract val updateServiceFactory: AppUpdateService.Factory

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        DaggerInjector.init(this, appSource, updateServiceFactory)
        DaggerInjector.appComponent.inject(this)

        initialDelegate.applyTheme()
        if (!BuildConfig.DEBUG) updateService.check()
    }
}