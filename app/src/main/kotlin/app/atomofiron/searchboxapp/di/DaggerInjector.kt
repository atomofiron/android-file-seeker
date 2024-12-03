package app.atomofiron.searchboxapp.di

import android.app.Application
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import app.atomofiron.searchboxapp.utils.AppWatcherProxy

object DaggerInjector {

    lateinit var appComponent: AppComponent

    fun init(application: Application) {
        appComponent = DaggerAppComponent
            .builder()
            .appContext(application.applicationContext)
            .initialStore(InitialDelegate(application.applicationContext))
            .appWatcher(AppWatcherProxy())
            .assetManager(application.assets)
            .packageManager(application.packageManager)
            .packageInstaller(application.packageManager.packageInstaller)
            .contentResolver(application.contentResolver)
            .build()
    }
}
