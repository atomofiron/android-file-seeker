package app.atomofiron.searchboxapp.di

import android.app.Application
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import app.atomofiron.searchboxapp.injectable.service.AppUpdateService
import debug.LeakWatcher

object DaggerInjector {

    lateinit var appComponent: AppComponent

    fun init(application: Application, updateServiceFactory: AppUpdateService.Factory) {
        appComponent = DaggerAppComponent
            .builder()
            .appContext(application.applicationContext)
            .initialDelegate(InitialDelegate(application.applicationContext))
            .updateServiceFactory(updateServiceFactory)
            .appWatcher(BuildConfig.leakWatcher ?: LeakWatcher())
            .assetManager(application.assets)
            .packageManager(application.packageManager)
            .packageInstaller(application.packageManager.packageInstaller)
            .contentResolver(application.contentResolver)
            .build()
    }
}
