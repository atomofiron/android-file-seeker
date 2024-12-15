package app.atomofiron.searchboxapp.screens.root

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class RootScope

@RootScope
@Component(dependencies = [RootDependencies::class], modules = [RootModule::class])
interface RootComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(view: WeakProperty<out Fragment>): Builder
        fun dependencies(dependencies: RootDependencies): Builder
        fun build(): RootComponent
    }

    fun inject(target: RootViewModel)
}

@Module
class RootModule {

    @Provides
    @RootScope
    fun presenter(
        scope: CoroutineScope,
        router: RootRouter,
    ): RootPresenter {
        return RootPresenter(scope, router)
    }

    @Provides
    @RootScope
    fun router(
        fragment: WeakProperty<out Fragment>,
        initialDelegate: InitialDelegate,
    ): RootRouter = RootRouter(fragment, initialDelegate)

    @Provides
    @RootScope
    fun viewState(scope: CoroutineScope): RootViewState = RootViewState(scope)
}

interface RootDependencies {
    fun initialDelegate(): InitialDelegate
}
