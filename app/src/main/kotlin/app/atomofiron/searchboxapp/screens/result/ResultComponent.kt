package app.atomofiron.searchboxapp.screens.result

import androidx.fragment.app.Fragment
import androidx.work.WorkManager
import app.atomofiron.common.util.ActivityProperty
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.module.DelegateModule
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.channel.ResultChannel
import app.atomofiron.searchboxapp.di.dependencies.interactor.ApkInteractor
import app.atomofiron.searchboxapp.di.dependencies.interactor.ResultInteractor
import app.atomofiron.searchboxapp.di.dependencies.router.FilePickingDelegate
import app.atomofiron.searchboxapp.di.dependencies.router.FileSharingDelegate
import app.atomofiron.searchboxapp.di.dependencies.service.ExplorerService
import app.atomofiron.searchboxapp.di.dependencies.service.FinderService
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.di.dependencies.store.ResultStore
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.common.delegates.FileOperationsDelegate
import app.atomofiron.searchboxapp.screens.result.presenter.ResultCurtainMenuDelegate
import app.atomofiron.searchboxapp.screens.result.presenter.ResultItemActionDelegate
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class ResultScope

@ResultScope
@Component(dependencies = [ResultDependencies::class], modules = [ResultModule::class, DelegateModule::class])
interface ResultComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(view: WeakProperty<out Fragment>): Builder
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(params: ResultPresenterParams): Builder
        @BindsInstance
        fun bind(mode: ActivityMode): Builder
        fun dependencies(dependencies: ResultDependencies): Builder
        fun build(): ResultComponent
    }

    fun inject(target: ResultViewModel)
}

@Module
class ResultModule {

    @Provides
    @ResultScope
    fun activity(property: WeakProperty<out Fragment>): ActivityProperty {
        return property.map { it?.activity }
    }

    @Provides
    @ResultScope
    fun presenter(
        params: ResultPresenterParams,
        scope: CoroutineScope,
        viewState: ResultViewState,
        finderStore: FinderStore,
        interactor: ResultInteractor,
        router: ResultRouter,
        resources: AppResources,
        itemActionDelegate: ResultItemActionDelegate,
        picking: FilePickingDelegate,
        sharing: FileSharingDelegate,
        workManager: WorkManager,
    ): ResultPresenter {
        return ResultPresenter(
            params,
            scope,
            viewState,
            finderStore,
            interactor,
            router,
            resources,
            itemActionDelegate,
            picking,
            sharing,
            workManager,
        )
    }

    @Provides
    @ResultScope
    fun resultItemActionDelegate(
        viewModel: ResultViewState,
        operations: FileOperationsDelegate,
        router: ResultRouter,
        menuListenerDelegate: ResultCurtainMenuDelegate,
        dialogs: DialogDelegate,
        interactor: ResultInteractor,
        sharing: FileSharingDelegate,
    ): ResultItemActionDelegate {
        return ResultItemActionDelegate(viewModel, operations, router, menuListenerDelegate, dialogs, interactor, sharing)
    }

    @Provides
    @ResultScope
    fun menuListenerDelegate(
        scope: CoroutineScope,
        router: ResultRouter,
        interactor: ResultInteractor,
        apks: ApkInteractor,
        curtainChannel: CurtainChannel,
        utils: UtilService,
        sharing: FileSharingDelegate,
    ): ResultCurtainMenuDelegate {
        return ResultCurtainMenuDelegate(scope, router, interactor, apks, curtainChannel, utils, sharing)
    }

    @Provides
    @ResultScope
    fun interactor(
        scope: CoroutineScope,
        utilService: UtilService,
        explorerService: ExplorerService,
        finderService: FinderService,
        finderStore: FinderStore,
        preferences: PreferenceStore,
    ): ResultInteractor {
        return ResultInteractor(scope, utilService, explorerService, finderService, finderStore, preferences)
    }

    @Provides
    @ResultScope
    fun router(fragment: WeakProperty<out Fragment>): ResultRouter = ResultRouter(fragment)

    @Provides
    @ResultScope
    fun viewState(
        params: ResultPresenterParams,
        mode: ActivityMode,
        scope: CoroutineScope,
        finderStore: FinderStore,
        preferenceStore: PreferenceStore,
    ): ResultViewState {
        return ResultViewState(params, mode, finderStore, scope, preferenceStore)
    }
}

interface ResultDependencies {
    fun appResources(): AppResources
    fun finderStore(): FinderStore
    fun preferenceStore(): PreferenceStore
    fun resultService(): UtilService
    fun explorerService(): ExplorerService
    fun finderService(): FinderService
    fun apkInteractor(): ApkInteractor
    fun resultStore(): ResultStore
    fun resultChannel(): ResultChannel
    fun curtainChannel(): CurtainChannel
    fun workManager(): WorkManager
}
