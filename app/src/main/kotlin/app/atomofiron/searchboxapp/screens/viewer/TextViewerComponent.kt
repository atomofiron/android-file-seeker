package app.atomofiron.searchboxapp.screens.viewer

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.interactor.TextViewerInteractor
import app.atomofiron.searchboxapp.injectable.service.TextViewerService
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.viewer.presenter.SearchAdapterPresenterDelegate
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
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
        textViewerInteractor: TextViewerInteractor,
        session: TextViewerSession,
    ): TextViewerPresenter {
        return TextViewerPresenter(
            params,
            scope,
            viewState,
            router,
            searchAdapterPresenterDelegate,
            textViewerInteractor,
            session,
        )
    }

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
        textViewerService: TextViewerService,
    ): TextViewerInteractor = TextViewerInteractor(scope, textViewerService)

    @Provides
    @TextViewerScope
    fun router(fragment: WeakProperty<out Fragment>): TextViewerRouter = TextViewerRouter(fragment)

    @Provides
    @TextViewerScope
    fun textViewerSession(
        params: TextViewerParams,
        interactor: TextViewerInteractor,
    ): TextViewerSession = interactor.fetchFileSession(params.path)

    @Provides
    @TextViewerScope
    fun viewerViewState(
        scope: CoroutineScope,
        session: TextViewerSession,
        preferenceStore: PreferenceStore,
    ): TextViewerViewState = TextViewerViewState(scope, session, preferenceStore)
}

interface TextViewerDependencies {
    fun preferenceStore(): PreferenceStore
    fun curtainChannel(): CurtainChannel
    fun textViewerService(): TextViewerService
}
