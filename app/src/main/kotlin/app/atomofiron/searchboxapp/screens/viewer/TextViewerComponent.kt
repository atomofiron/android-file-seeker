package app.atomofiron.searchboxapp.screens.viewer

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.di.dependencies.store.TextViewerStore
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.viewer.di.TextViewerInteractor
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNodeError
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
    val error: NodeError? = result.err()?.message?.toNodeError()
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
    fun textViewerSession(
        params: TextViewerParams,
        interactor: TextViewerInteractor,
    ): TextViewerSessionResult = TextViewerSessionResult(interactor.fetchFileSession(params.ref))
}

interface TextViewerDependencies {
    fun preferenceStore(): PreferenceStore
    fun textViewerStore(): TextViewerStore
    fun explorerStore(): ExplorerStore
    fun finderStore(): FinderStore
    fun curtainChannel(): CurtainChannel
    fun utilService(): UtilService
}
