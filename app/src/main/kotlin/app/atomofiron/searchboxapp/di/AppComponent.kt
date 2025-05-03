package app.atomofiron.searchboxapp.di

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.content.res.AssetManager
import app.atomofiron.searchboxapp.android.AbstractApp
import app.atomofiron.searchboxapp.android.InstallReceiver
import app.atomofiron.searchboxapp.di.module.ChannelModule
import app.atomofiron.searchboxapp.di.module.CommonModule
import app.atomofiron.searchboxapp.di.module.DelegateModule
import app.atomofiron.searchboxapp.di.module.InteractorModule
import app.atomofiron.searchboxapp.di.module.ServiceModule
import app.atomofiron.searchboxapp.di.module.StoreModule
import app.atomofiron.searchboxapp.injectable.service.AppUpdateService
import app.atomofiron.searchboxapp.model.AppSource
import app.atomofiron.searchboxapp.screens.curtain.CurtainDependencies
import app.atomofiron.searchboxapp.screens.explorer.ExplorerDependencies
import app.atomofiron.searchboxapp.screens.finder.FinderDependencies
import app.atomofiron.searchboxapp.screens.main.MainDependencies
import app.atomofiron.searchboxapp.screens.preferences.PreferenceDependencies
import app.atomofiron.searchboxapp.screens.result.ResultDependencies
import app.atomofiron.searchboxapp.screens.root.RootDependencies
import app.atomofiron.searchboxapp.screens.template.TemplateDependencies
import app.atomofiron.searchboxapp.screens.viewer.TextViewerDependencies
import app.atomofiron.searchboxapp.work.FinderWorker
import dagger.BindsInstance
import dagger.Component
import debug.LeakWatcher
import javax.inject.Singleton

@Component(modules = [
    ChannelModule::class,
    CommonModule::class,
    DelegateModule::class,
    ServiceModule::class,
    StoreModule::class,
    InteractorModule::class,
    //NetworkModule::class, I'll be back...
])
@Singleton
interface AppComponent :
    MainDependencies,
    RootDependencies,
    CurtainDependencies,
    PreferenceDependencies,
    ExplorerDependencies,
    FinderDependencies,
    ResultDependencies,
    TextViewerDependencies,
    TemplateDependencies
{

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun appSource(appSource: AppSource): Builder

        @BindsInstance
        fun appContext(context: Context): Builder

        @BindsInstance
        fun appWatcher(proxy: LeakWatcher): Builder

        @BindsInstance
        fun updateServiceFactory(updateServiceFactory: AppUpdateService.Factory): Builder

        @BindsInstance
        fun assetManager(assetManager: AssetManager): Builder

        @BindsInstance
        fun packageManager(packageManager: PackageManager): Builder

        @BindsInstance
        fun packageInstaller(packageInstaller: PackageInstaller): Builder

        @BindsInstance
        fun contentResolver(contentResolver: ContentResolver): Builder // unused?

        fun build(): AppComponent
    }

    fun inject(target: AbstractApp)
    fun inject(target: FinderWorker)
    fun inject(target: InstallReceiver)
}
