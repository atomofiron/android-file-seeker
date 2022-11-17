package app.atomofiron.searchboxapp.screens.finder

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.interactor.FinderInteractor
import app.atomofiron.searchboxapp.injectable.service.FinderService
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.finder.presenter.FinderAdapterPresenterDelegate
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class FinderScope

@FinderScope
@Component(dependencies = [FinderDependencies::class], modules = [FinderModule::class])
interface FinderComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(fragment: WeakProperty<Fragment>): Builder
        fun dependencies(dependencies: FinderDependencies): Builder
        fun build(): FinderComponent
    }

    fun inject(target: FinderViewModel)
}

@Module
class FinderModule {
    @Provides
    @FinderScope
    fun presenter(
        scope: CoroutineScope,
        viewState: FinderViewState,
        router: FinderRouter,
        finderAdapterDelegate: FinderAdapterPresenterDelegate,
        finderStore: FinderStore,
        explorerStore: ExplorerStore,
        preferenceStore: PreferenceStore,
        preferenceChannel: PreferenceChannel
    ): FinderPresenter {
        return FinderPresenter(
            scope,
            viewState,
            router,
            finderAdapterDelegate,
            explorerStore,
            preferenceStore,
            finderStore,
            preferenceChannel,
        )
    }

    @Provides
    @FinderScope
    fun finderAdapterOutput(
        viewState: FinderViewState,
        router: FinderRouter,
        interactor: FinderInteractor,
    ): FinderAdapterPresenterDelegate {
        return FinderAdapterPresenterDelegate(viewState, router, interactor)
    }

    @Provides
    @FinderScope
    fun router(fragment: WeakProperty<Fragment>) = FinderRouter(fragment)

    @Provides
    @FinderScope
    fun interactor(finderService: FinderService): FinderInteractor {
        return FinderInteractor(finderService)
    }

    @Provides
    @FinderScope
    fun viewState(scope: CoroutineScope): FinderViewState = FinderViewState(scope)
}

interface FinderDependencies {
    fun preferenceChannel(): PreferenceChannel
    fun explorerStore(): ExplorerStore
    fun preferenceStore(): PreferenceStore
    fun finderService(): FinderService
    fun finderStore(): FinderStore
}
