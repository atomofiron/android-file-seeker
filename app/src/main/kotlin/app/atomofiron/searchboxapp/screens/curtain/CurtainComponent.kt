package app.atomofiron.searchboxapp.screens.curtain

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainPresenterParams
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class CurtainScope

@CurtainScope
@Component(dependencies = [CurtainDependencies::class], modules = [CurtainModule::class])
interface CurtainComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(view: WeakProperty<out Fragment>): Builder
        @BindsInstance
        fun bind(params: CurtainPresenterParams): Builder
        fun dependencies(dependencies: CurtainDependencies): Builder
        fun build(): CurtainComponent
    }

    fun inject(target: CurtainViewModel)
}

@Module
class CurtainModule {

    @Provides
    @CurtainScope
    fun presenter(
        params: CurtainPresenterParams,
        viewState: CurtainViewState,
        router: CurtainRouter,
        channel: CurtainChannel,
    ): CurtainPresenter {
        return CurtainPresenter(params, viewState, router, channel)
    }

    @Provides
    @CurtainScope
    fun router(fragment: WeakProperty<out Fragment>): CurtainRouter = CurtainRouter(fragment)

    @Provides
    @CurtainScope
    fun viewState(
        params: CurtainPresenterParams,
        scope: CoroutineScope,
    ): CurtainViewState = CurtainViewState(params, scope)
}

interface CurtainDependencies {
    fun curtainChannel(): CurtainChannel
}
