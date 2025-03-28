package app.atomofiron.searchboxapp.screens.explorer

import android.content.Context
import android.content.res.AssetManager
import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.channel.MainChannel
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.interactor.DialogInteractor
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.injectable.service.UtilService
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
        router: ExplorerRouter,
        explorerInteractor: ExplorerInteractor,
        apks: ApkInteractor,
        dialogs: DialogInteractor,
        preferences: PreferenceStore,
    ): ExplorerItemActionListenerDelegate {
        return ExplorerItemActionListenerDelegate(
            viewState,
            menuListenerDelegate,
            explorerStore,
            router,
            explorerInteractor,
            apks,
            dialogs,
            preferences,
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
        apkInteractor: ApkInteractor,
        preferences: PreferenceStore,
        curtainChannel: CurtainChannel,
    ): ExplorerCurtainMenuDelegate {
        return ExplorerCurtainMenuDelegate(scope, viewState, router, explorerStore, explorerInteractor, apkInteractor, preferences, curtainChannel)
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
            mainChannel,
        )
    }

    @Provides
    @ExplorerScope
    fun interactor(scope: CoroutineScope, explorerService: ExplorerService, utils: UtilService): ExplorerInteractor {
        return ExplorerInteractor(scope, explorerService, utils)
    }

    @Provides
    @ExplorerScope
    fun router(fragment: WeakProperty<out Fragment>): ExplorerRouter {
        return ExplorerRouter(fragment)
    }

    @Provides
    @ExplorerScope
    fun viewState(
        scope: CoroutineScope,
        explorerStore: ExplorerStore,
        interactor: ExplorerInteractor,
        preferenceChannel: PreferenceChannel,
    ): ExplorerViewState {
        return ExplorerViewState(scope, explorerStore, interactor, preferenceChannel)
    }
}

interface ExplorerDependencies {
    fun context(): Context
    fun assetManager(): AssetManager
    fun explorerService(): ExplorerService
    fun utilService(): UtilService
    fun explorerStore(): ExplorerStore
    fun preferenceStore(): PreferenceStore
    fun curtainChannel(): CurtainChannel
    fun apkInteractor(): ApkInteractor
    fun dialogInteractor(): DialogInteractor
    fun mainChannel(): MainChannel
    fun preferenceChannel(): PreferenceChannel
}
