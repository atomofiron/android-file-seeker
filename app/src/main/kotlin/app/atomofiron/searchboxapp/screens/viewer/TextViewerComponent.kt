package app.atomofiron.searchboxapp.screens.viewer

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.interactor.TextViewerInteractor
import app.atomofiron.searchboxapp.di.dependencies.service.TextViewerService
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.di.dependencies.store.TextViewerStore
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.viewer.presenter.SearchAdapterPresenterDelegate
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
import app.atomofiron.searchboxapp.utils.Rslt
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class TextViewerScope

class TextViewerSessionResult(val result: Rslt<TextViewerSession>) {
    val error: String? = (result as? Rslt.Err)?.message
}

@TextViewerScope
@Component(dependencies = [TextViewerDependencies::class], modules = [TextViewerModule::class])
interface TextViewerComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(params: TextViewerParams): Builder
        @BindsInstance
        fun bind(view: WeakProperty<out Fragment>): Builder
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        fun dependencies(dependencies: TextViewerDependencies): Builder
        fun build(): TextViewerComponent
    }

    fun inject(target: TextViewerViewModel)
}

@Module
class TextViewerModule {

    @Provides
    @TextViewerScope
    fun presenter(
        params: TextViewerParams,
        scope: CoroutineScope,
        viewState: TextViewerViewState,
        router: TextViewerRouter,
        searchAdapterPresenterDelegate: SearchAdapterPresenterDelegate,
        interactor: TextViewerInteractor,
        session: TextViewerSessionResult,
    ): TextViewerPresenter = TextViewerPresenter(
        params,
        scope,
        viewState,
        router,
        searchAdapterPresenterDelegate,
        interactor,
        session.result.value,
    )

    @Provides
    @TextViewerScope
    fun searchOutputDelegate(
        scope: CoroutineScope,
        viewState: TextViewerViewState,
        router: TextViewerRouter,
        interactor: TextViewerInteractor,
        preferenceStore: PreferenceStore,
        curtainChannel: CurtainChannel,
    ): SearchAdapterPresenterDelegate {
        return SearchAdapterPresenterDelegate(scope, viewState, router, interactor, preferenceStore, curtainChannel)
    }

    @Provides
    @TextViewerScope
    fun textViewerInteractor(
        scope: CoroutineScope,
        service: TextViewerService,
        explorerStore: ExplorerStore,
        preferences: PreferenceStore,
        utils: UtilService,
    ): TextViewerInteractor = TextViewerInteractor(scope, service, explorerStore, utils, preferences)

    @Provides
    @TextViewerScope
    fun router(fragment: WeakProperty<out Fragment>): TextViewerRouter = TextViewerRouter(fragment)

    @Provides
    @TextViewerScope
    fun textViewerSession(
        params: TextViewerParams,
        interactor: TextViewerInteractor,
    ): TextViewerSessionResult = TextViewerSessionResult(interactor.fetchFileSession(params.ref))

    @Provides
    @TextViewerScope
    fun viewerViewState(
        params: TextViewerParams,
        scope: CoroutineScope,
        session: TextViewerSessionResult,
        preferenceStore: PreferenceStore,
    ): TextViewerViewState = TextViewerViewState(params.ref, scope, session.result.value, preferenceStore, session.error)

    @Provides
    @TextViewerScope
    fun service(
        scope: CoroutineScope,
        preferenceStore: PreferenceStore,
        textViewerStore: TextViewerStore,
        finderStore: FinderStore,
    ): TextViewerService = TextViewerService(scope, preferenceStore, textViewerStore, finderStore)
}

interface TextViewerDependencies {
    fun preferenceStore(): PreferenceStore
    fun textViewerStore(): TextViewerStore
    fun explorerStore(): ExplorerStore
    fun finderStore(): FinderStore
    fun curtainChannel(): CurtainChannel
    fun utilService(): UtilService
}
