package app.atomofiron.searchboxapp.screens.viewer

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import app.atomofiron.searchboxapp.injectable.channel.TextViewerChannel
import app.atomofiron.searchboxapp.injectable.interactor.TextViewerInteractor
import app.atomofiron.searchboxapp.injectable.service.TextViewerService
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.viewer.presenter.SearchAdapterPresenterDelegate
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
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
        preferenceStore: PreferenceStore,
        textViewerChannel: TextViewerChannel
    ): TextViewerPresenter {
        return TextViewerPresenter(
            params,
            scope,
            viewState,
            router,
            searchAdapterPresenterDelegate,
            textViewerInteractor,
            preferenceStore,
            textViewerChannel,
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
    fun textViewerService(
        textViewerChannel: TextViewerChannel,
        preferenceStore: PreferenceStore
    ): TextViewerService = TextViewerService(textViewerChannel, preferenceStore)

    @Provides
    @TextViewerScope
    fun textViewerInteractor(
        scope: CoroutineScope,
        textViewerService: TextViewerService
    ): TextViewerInteractor = TextViewerInteractor(scope, textViewerService)

    @Provides
    @TextViewerScope
    fun textViewerChannel(): TextViewerChannel = TextViewerChannel()

    @Provides
    @TextViewerScope
    fun router(fragment: WeakProperty<out Fragment>): TextViewerRouter = TextViewerRouter(fragment)

    @Provides
    @TextViewerScope
    fun viewerViewState(scope: CoroutineScope): TextViewerViewState = TextViewerViewState(scope)
}

interface TextViewerDependencies {
    fun preferenceStore(): PreferenceStore
    fun curtainChannel(): CurtainChannel
}
