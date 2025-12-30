package app.atomofiron.searchboxapp.screens.curtain

import androidx.fragment.app.Fragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainParams
import dagger.BindsInstance
import dagger.Component
import dagger.Module
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
        fun bind(params: CurtainParams): Builder
        fun dependencies(dependencies: CurtainDependencies): Builder
        fun build(): CurtainComponent
    }

    fun inject(target: CurtainViewModel)
}

@Module
class CurtainModule

interface CurtainDependencies {
    fun curtainChannel(): CurtainChannel
}
