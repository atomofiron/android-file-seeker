package app.atomofiron.searchboxapp.screens.explorer

import android.content.Context
import android.content.res.AssetManager
import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.channel.MainChannel
import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.router.FileSharingDelegateImpl
import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerCurtainMenuDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerItemActionListenerDelegate
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class ExplorerScope

@ExplorerScope
@Component(dependencies = [ExplorerDependencies::class], modules = [ExplorerModule::class])
interface ExplorerComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(view: WeakProperty<out Fragment>): Builder
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        fun dependencies(dependencies: ExplorerDependencies): Builder
        fun build(): ExplorerComponent
    }

    fun inject(target: ExplorerViewModel)
}

@Module
class ExplorerModule {
    @Provides
    @ExplorerScope
    fun itemListener(
        viewState: ExplorerViewState,
        menuListenerDelegate: ExplorerCurtainMenuDelegate,
        explorerStore: ExplorerStore,
        preferenceStore: PreferenceStore,
        router: ExplorerRouter,
        explorerInteractor: ExplorerInteractor,
        apkInteractor: ApkInteractor,
    ): ExplorerItemActionListenerDelegate {
        return ExplorerItemActionListenerDelegate(
            viewState,
            menuListenerDelegate,
            explorerStore,
            preferenceStore,
            router,
            explorerInteractor,
            apkInteractor,
        )
    }

    @Provides
    @ExplorerScope
    fun menuListener(
        scope: CoroutineScope,
        viewState: ExplorerViewState,
        router: ExplorerRouter,
        explorerStore: ExplorerStore,
        explorerInteractor: ExplorerInteractor,
        curtainChannel: CurtainChannel,
    ): ExplorerCurtainMenuDelegate {
        return ExplorerCurtainMenuDelegate(scope, viewState, router, explorerStore, explorerInteractor, curtainChannel)
    }

    @Provides
    @ExplorerScope
    fun presenter(
        scope: CoroutineScope,
        viewState: ExplorerViewState,
        router: ExplorerRouter,
        explorerStore: ExplorerStore,
        preferenceStore: PreferenceStore,
        explorerInteractor: ExplorerInteractor,
        itemListener: ExplorerItemActionListenerDelegate,
        curtainMenuDelegate: ExplorerCurtainMenuDelegate,
        mainChannel: MainChannel,
    ): ExplorerPresenter {
        return ExplorerPresenter(
            scope,
            viewState,
            router,
            explorerStore,
            preferenceStore,
            explorerInteractor,
            itemListener,
            curtainMenuDelegate,
            mainChannel,
        )
    }

    @Provides
    @ExplorerScope
    fun interactor(scope: CoroutineScope, explorerService: ExplorerService): ExplorerInteractor {
        return ExplorerInteractor(scope, explorerService)
    }

    @Provides
    @ExplorerScope
    fun router(fragment: WeakProperty<out Fragment>, appStore: AppStore): ExplorerRouter {
        return ExplorerRouter(fragment, FileSharingDelegateImpl(appStore))
    }

    @Provides
    @ExplorerScope
    fun viewState(scope: CoroutineScope, explorerStore: ExplorerStore): ExplorerViewState {
        return ExplorerViewState(scope, explorerStore)
    }
}

interface ExplorerDependencies {
    fun context(): Context
    fun assetManager(): AssetManager
    fun explorerService(): ExplorerService
    fun explorerStore(): ExplorerStore
    fun preferenceStore(): PreferenceStore
    fun appStore(): AppStore
    fun curtainChannel(): CurtainChannel
    fun apkInteractor(): ApkInteractor
    fun mainChannel(): MainChannel
}
