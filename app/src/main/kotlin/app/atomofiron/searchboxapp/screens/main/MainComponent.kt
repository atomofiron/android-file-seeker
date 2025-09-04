package app.atomofiron.searchboxapp.screens.main

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import app.atomofiron.common.util.ActivityProperty
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.module.DelegateModule
import app.atomofiron.searchboxapp.di.dependencies.channel.ApkChannel
import app.atomofiron.searchboxapp.di.dependencies.channel.MainChannel
import app.atomofiron.searchboxapp.di.dependencies.delegate.InitialDelegate
import app.atomofiron.searchboxapp.di.dependencies.interactor.ApkInteractor
import app.atomofiron.searchboxapp.di.dependencies.service.AppUpdateService
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.service.WindowService
import app.atomofiron.searchboxapp.di.dependencies.store.AndroidStore
import app.atomofiron.searchboxapp.di.dependencies.store.AppStore
import app.atomofiron.searchboxapp.di.dependencies.store.AppStoreConsumer
import app.atomofiron.searchboxapp.di.dependencies.store.AppUpdateStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.common.delegates.FileOperationsDelegate
import app.atomofiron.searchboxapp.screens.main.presenter.AppEventDelegate
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
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
class MainModule {

    @Provides
    @MainScope
    fun activity(property: WeakProperty<out FragmentActivity>): ActivityProperty {
        return property
    }

    @Provides
    @MainScope
    fun presenter(
        scope: CoroutineScope,
        viewState: MainViewState,
        router: MainRouter,
        appEventDelegate: AppEventDelegate,
        windowService: WindowService,
        preferenceStore: PreferenceStore,
        initialDelegate: InitialDelegate,
    ): MainPresenter {
        return MainPresenter(scope, viewState, router, appEventDelegate, windowService, preferenceStore, initialDelegate)
    }

    @Provides
    @MainScope
    fun appEventDelegate(
        context: Context,
        scope: CoroutineScope,
        router: MainRouter,
        operations: FileOperationsDelegate,
        dialogs: DialogDelegate,
        appStoreConsumer: AppStoreConsumer,
        preferenceStore: PreferenceStore,
        updateStore: AppUpdateStore,
        mainChannel: MainChannel,
        apkChannel: ApkChannel,
        updateService: AppUpdateService,
    ): AppEventDelegate {
        return AppEventDelegate(context, scope, router, appStoreConsumer, operations, dialogs, preferenceStore, updateStore, mainChannel, apkChannel, updateService)
    }

    @Provides
    @MainScope
    fun router(activity: WeakProperty<out FragmentActivity>): MainRouter = MainRouter(activity)

    @Provides
    @MainScope
    fun viewState(
        activityMode: ActivityMode,
        preferenceStore: PreferenceStore,
        initialDelegate: InitialDelegate,
    ): MainViewState = MainViewState(activityMode, preferenceStore, initialDelegate)

    @Provides
    @MainScope
    fun provideAndroidStore(activity: AppCompatActivity): AndroidStore {
        return AndroidStore(activity)
    }

    @Provides
    @MainScope
    fun provideAppStore(androidStore: AndroidStore): AppStore {
        return androidStore
    }

    @Provides
    @MainScope
    fun provideAppStoreConsumer(androidStore: AndroidStore): AppStoreConsumer {
        return androidStore
    }

    @Provides
    @MainScope
    fun windowService(
        appStore: AppStore,
    ): WindowService = WindowService(appStore)
}

interface MainDependencies {
    fun context(): Context
    fun preferenceStore(): PreferenceStore
    fun mainChannel(): MainChannel
    fun apkChannel (): ApkChannel
    fun initialDelegate(): InitialDelegate
    fun appUpdateService(): AppUpdateService
    fun appUpdateStore(): AppUpdateStore
    fun apks(): ApkInteractor
    fun utils(): UtilService
}
