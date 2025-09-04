package app.atomofiron.searchboxapp.screens.template

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class TemplateScope

@TemplateScope
@Component(dependencies = [TemplateDependencies::class], modules = [TemplateModule::class])
interface TemplateComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(view: WeakProperty<out Fragment>): Builder
        fun dependencies(dependencies: TemplateDependencies): Builder
        fun build(): TemplateComponent
    }

    fun inject(target: TemplateViewModel)
}

@Module
class TemplateModule {

    @Provides
    @TemplateScope
    fun presenter(
        scope: CoroutineScope,
        router: TemplateRouter,
        preferenceStore: PreferenceStore,
    ): TemplatePresenter {
        return TemplatePresenter(scope, router, preferenceStore)
    }

    @Provides
    @TemplateScope
    fun router(
        fragment: WeakProperty<out Fragment>,
        preferenceStore: PreferenceStore,
    ): TemplateRouter = TemplateRouter(fragment, preferenceStore)

    @Provides
    @TemplateScope
    fun viewState(): TemplateViewState = TemplateViewState()
}

interface TemplateDependencies {
    fun preferenceStore(): PreferenceStore
}
