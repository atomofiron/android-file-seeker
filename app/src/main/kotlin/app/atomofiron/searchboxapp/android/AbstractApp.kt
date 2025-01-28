package app.atomofiron.searchboxapp.android

import android.app.Application
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import app.atomofiron.searchboxapp.injectable.service.AppUpdateService
import com.google.android.material.color.DynamicColors
import javax.inject.Inject

abstract class AbstractApp : Application() {

    @Inject
    lateinit var initialDelegate: InitialDelegate

    @Inject
    lateinit var updateService: AppUpdateService

    protected abstract val updateServiceFactory: AppUpdateService.Factory

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        DaggerInjector.init(this, updateServiceFactory)
        DaggerInjector.appComponent.inject(this)

        initialDelegate.applyTheme()
        updateService.check()
    }
}