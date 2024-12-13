package app.atomofiron.searchboxapp.android

import android.app.Application
import androidx.work.Configuration
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import app.atomofiron.searchboxapp.injectable.service.AppUpdateService
import com.google.android.material.color.DynamicColors
import javax.inject.Inject

class App : Application(), Configuration.Provider {

    override val workManagerConfiguration = Configuration.Builder().build()

    @Inject
    lateinit var initialDelegate: InitialDelegate

    @Inject
    lateinit var updateService: AppUpdateService

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)

        DaggerInjector.init(this)
        DaggerInjector.appComponent.inject(this)

        initialDelegate.applyTheme()
        updateService.check()
    }
}