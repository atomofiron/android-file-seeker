package app.atomofiron.searchboxapp.screens.main

import androidx.fragment.app.FragmentActivity
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.channel.MainChannel
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import app.atomofiron.searchboxapp.injectable.service.AppUpdateService
import app.atomofiron.searchboxapp.injectable.service.WindowService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.AppUpdateStore
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.main.presenter.AppEventDelegate
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class MainScope

@MainScope
@Component(dependencies = [MainDependencies::class], modules = [MainModule::class])
interface MainComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(view: WeakProperty<out FragmentActivity>): Builder
        fun dependencies(dependencies: MainDependencies): Builder
        fun build(): MainComponent
    }

    fun inject(target: MainViewModel)
}

@Module
class MainModule {

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
        scope: CoroutineScope,
        router: MainRouter,
        appStore: AppStore,
        preferenceStore: PreferenceStore,
        updateStore: AppUpdateStore,
        mainChannel: MainChannel,
        updateService: AppUpdateService,
    ): AppEventDelegate {
        return AppEventDelegate(scope, router, appStore, preferenceStore, updateStore, mainChannel, updateService)
    }

    @Provides
    @MainScope
    fun router(activity: WeakProperty<out FragmentActivity>): MainRouter = MainRouter(activity)

    @Provides
    @MainScope
    fun viewState(
        scope: CoroutineScope,
        preferenceStore: PreferenceStore,
        initialDelegate: InitialDelegate,
    ): MainViewState = MainViewState(scope, preferenceStore, initialDelegate)
}

interface MainDependencies {
    fun windowService(): WindowService
    fun appStore(): AppStore
    fun preferenceStore(): PreferenceStore
    fun mainChannel(): MainChannel
    fun initialDelegate(): InitialDelegate
    fun appUpdateService(): AppUpdateService
    fun appUpdateStore(): AppUpdateStore
}
