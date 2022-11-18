package app.atomofiron.searchboxapp.screens.main

import androidx.fragment.app.FragmentActivity
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.channel.MainChannel
import app.atomofiron.searchboxapp.injectable.service.WindowService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
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
        windowService: WindowService,
        appStore: AppStore,
        preferenceStore: PreferenceStore,
        mainChannel: MainChannel,
    ): MainPresenter {
        return MainPresenter(scope, viewState, router, windowService, appStore, preferenceStore, mainChannel)
    }

    @Provides
    @MainScope
    fun router(activity: WeakProperty<out FragmentActivity>): MainRouter = MainRouter(activity)

    @Provides
    @MainScope
    fun viewState(scope: CoroutineScope, preferenceStore: PreferenceStore): MainViewState = MainViewState(scope, preferenceStore)
}

interface MainDependencies {
    fun windowService(): WindowService
    fun appStore(): AppStore
    fun preferenceStore(): PreferenceStore
    fun mainChannel(): MainChannel
}
