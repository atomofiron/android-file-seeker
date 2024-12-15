package app.atomofiron.searchboxapp.di

import android.app.Application
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import debug.AppWatcherProxy

object DaggerInjector {

    lateinit var appComponent: AppComponent

    fun init(application: Application) {
        appComponent = DaggerAppComponent
            .builder()
            .appContext(application.applicationContext)
            .initialStore(InitialDelegate(application.applicationContext))
            .appWatcher(BuildConfig.appWatcher ?: AppWatcherProxy())
            .assetManager(application.assets)
            .packageManager(application.packageManager)
            .packageInstaller(application.packageManager.packageInstaller)
            .contentResolver(application.contentResolver)
            .build()
    }
}
