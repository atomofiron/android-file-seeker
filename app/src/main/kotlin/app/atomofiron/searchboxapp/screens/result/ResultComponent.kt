package app.atomofiron.searchboxapp.screens.result

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import app.atomofiron.searchboxapp.injectable.channel.ResultChannel
import app.atomofiron.searchboxapp.injectable.interactor.ResultInteractor
import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.injectable.service.ResultService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.injectable.store.ResultStore
import app.atomofiron.searchboxapp.screens.result.presenter.ResultCurtainMenuDelegate
import app.atomofiron.searchboxapp.screens.result.presenter.ResultItemActionDelegate
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class ResultScope

@ResultScope
@Component(dependencies = [ResultDependencies::class], modules = [ResultModule::class])
interface ResultComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(view: WeakProperty<out Fragment>): Builder
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(params: ResultPresenterParams): Builder
        fun dependencies(dependencies: ResultDependencies): Builder
        fun build(): ResultComponent
    }

    fun inject(target: ResultViewModel)
}

@Module
class ResultModule {

    @Provides
    @ResultScope
    fun presenter(
        params: ResultPresenterParams,
        scope: CoroutineScope,
        viewState: ResultViewState,
        finderStore: FinderStore,
        preferenceStore: PreferenceStore,
        interactor: ResultInteractor,
        router: ResultRouter,
        appStore: AppStore,
        itemActionDelegate: ResultItemActionDelegate,
    ): ResultPresenter {
        return ResultPresenter(
            params,
            scope,
            viewState,
            finderStore,
            preferenceStore,
            interactor,
            router,
            appStore,
            itemActionDelegate,
        )
    }

    @Provides
    @ResultScope
    fun resultItemActionDelegate(
        viewModel: ResultViewState,
        router: ResultRouter,
        menuListenerDelegate: ResultCurtainMenuDelegate,
        interactor: ResultInteractor,
        preferenceStore: PreferenceStore,
    ): ResultItemActionDelegate {
        return ResultItemActionDelegate(viewModel, router, menuListenerDelegate, interactor, preferenceStore)
    }

    @Provides
    @ResultScope
    fun menuListenerDelegate(
        scope: CoroutineScope,
        viewState: ResultViewState,
        router: ResultRouter,
        interactor: ResultInteractor,
        appStore: AppStore,
        curtainChannel: CurtainChannel,
    ): ResultCurtainMenuDelegate {
        return ResultCurtainMenuDelegate(scope, viewState, router, interactor, appStore, curtainChannel)
    }

    @Provides
    @ResultScope
    fun interactor(
        scope: CoroutineScope,
        resultService: ResultService,
        explorerService: ExplorerService,
    ): ResultInteractor {
        return ResultInteractor(scope, resultService, explorerService)
    }

    @Provides
    @ResultScope
    fun router(fragment: WeakProperty<out Fragment>): ResultRouter = ResultRouter(fragment)

    @Provides
    @ResultScope
    fun viewState(scope: CoroutineScope): ResultViewState = ResultViewState(scope)
}

interface ResultDependencies {
    fun finderStore(): FinderStore
    fun preferenceStore(): PreferenceStore
    fun resultService(): ResultService
    fun explorerService(): ExplorerService
    fun resultStore(): ResultStore
    fun resultChannel(): ResultChannel
    fun appStore(): AppStore
    fun curtainChannel(): CurtainChannel
}
