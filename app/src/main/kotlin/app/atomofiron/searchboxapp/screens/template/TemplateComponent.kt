package app.atomofiron.searchboxapp.screens.template

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import dagger.Binds
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
interface TemplateModule {
    @Binds
    @TemplateScope
    fun dep(impl: Any): Any
}

interface TemplateDependencies {
    fun preferences(): PreferenceStore
}
