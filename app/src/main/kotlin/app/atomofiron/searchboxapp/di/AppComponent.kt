package app.atomofiron.searchboxapp.di

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.content.res.AssetManager
import app.atomofiron.searchboxapp.android.App
import app.atomofiron.searchboxapp.di.module.ChannelModule
import app.atomofiron.searchboxapp.di.module.CommonModule
import app.atomofiron.searchboxapp.di.module.InteractorModule
import app.atomofiron.searchboxapp.di.module.NetworkModule
import app.atomofiron.searchboxapp.di.module.ServiceModule
import app.atomofiron.searchboxapp.di.module.StoreModule
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
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
    ServiceModule::class,
    StoreModule::class,
    InteractorModule::class,
    NetworkModule::class,
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
        fun appContext(context: Context): Builder

        @BindsInstance
        fun appWatcher(proxy: LeakWatcher): Builder

        @BindsInstance
        fun initialDelegate(initialDelegate: InitialDelegate): Builder

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

    fun inject(target: App)
    fun inject(target: FinderWorker)
}
