package app.atomofiron.searchboxapp.di

import android.app.Application
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.di.dependencies.delegate.InitialDelegate
import app.atomofiron.searchboxapp.di.dependencies.service.AppUpdateService
import app.atomofiron.searchboxapp.model.AppSource
import debug.LeakWatcher

object DaggerInjector {

    lateinit var appComponent: AppComponent

    fun init(application: Application, appSource: AppSource, updateServiceFactory: AppUpdateService.Factory) {
        appComponent = DaggerAppComponent
            .builder()
            .appSource(appSource)
            .appContext(application.applicationContext)
            .updateServiceFactory(updateServiceFactory)
            .appWatcher(BuildConfig.leakWatcher ?: LeakWatcher())
            .assetManager(application.assets)
            .packageManager(application.packageManager)
            .packageInstaller(application.packageManager.packageInstaller)
            .contentResolver(application.contentResolver)
            .build()
    }
}
