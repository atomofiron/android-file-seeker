package app.atomofiron.searchboxapp.di

import android.app.Application
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import debug.LeakWatcher

object DaggerInjector {

    lateinit var appComponent: AppComponent

    fun init(application: Application) {
        appComponent = DaggerAppComponent
            .builder()
            .appContext(application.applicationContext)
            .initialDelegate(InitialDelegate(application.applicationContext))
            .appWatcher(BuildConfig.leakWatcher ?: LeakWatcher())
            .assetManager(application.assets)
            .packageManager(application.packageManager)
            .packageInstaller(application.packageManager.packageInstaller)
            .contentResolver(application.contentResolver)
            .build()
    }
}
