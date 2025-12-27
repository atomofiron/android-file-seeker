package app.atomofiron.searchboxapp.screens.main

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import app.atomofiron.common.util.ActivityProperty
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.dependencies.channel.ApkChannel
import app.atomofiron.searchboxapp.di.dependencies.delegate.InitialDelegate
import app.atomofiron.searchboxapp.di.dependencies.interactor.ApkInteractor
import app.atomofiron.searchboxapp.di.dependencies.service.AppUpdateService
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.store.AndroidStore
import app.atomofiron.searchboxapp.di.dependencies.store.AppStore
import app.atomofiron.searchboxapp.di.dependencies.store.AppStoreConsumer
import app.atomofiron.searchboxapp.di.dependencies.store.AppUpdateStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.di.module.DelegateModule
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class MainScope

@MainScope
@Component(dependencies = [MainDependencies::class], modules = [MainModule::class, DelegateModule::class])
interface MainComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(view: WeakProperty<out FragmentActivity>): Builder
        @BindsInstance
        fun bind(activity: AppCompatActivity): Builder
        @BindsInstance
        fun bind(activityMode: ActivityMode): Builder
        fun dependencies(dependencies: MainDependencies): Builder
        fun build(): MainComponent
    }

    fun inject(target: MainViewModel)
}

@Module
abstract class MainModule {

    @Binds
    @MainScope
    abstract fun activity(property: WeakProperty<out FragmentActivity>): ActivityProperty

    @Binds
    @MainScope
    abstract fun provideAppStore(androidStore: AndroidStore): AppStore

    @Binds
    @MainScope
    abstract fun provideAppStoreConsumer(androidStore: AndroidStore): AppStoreConsumer
}

interface MainDependencies {
    fun context(): Context
    fun preferenceStore(): PreferenceStore
    fun apkChannel (): ApkChannel
    fun initialDelegate(): InitialDelegate
    fun appUpdateService(): AppUpdateService
    fun appUpdateStore(): AppUpdateStore
    fun apks(): ApkInteractor
    fun utils(): UtilService
}
