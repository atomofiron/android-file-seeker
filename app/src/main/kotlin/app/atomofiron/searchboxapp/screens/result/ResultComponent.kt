package app.atomofiron.searchboxapp.screens.result

import androidx.fragment.app.Fragment
import androidx.work.WorkManager
import app.atomofiron.common.util.ActivityProperty
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.channel.ResultChannel
import app.atomofiron.searchboxapp.di.dependencies.delegate.ApkDelegate
import app.atomofiron.searchboxapp.di.dependencies.service.ExplorerService
import app.atomofiron.searchboxapp.di.dependencies.service.FinderService
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.ResultStore
import app.atomofiron.searchboxapp.di.module.DelegateModule
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class ResultScope

@ResultScope
@Component(dependencies = [ResultDependencies::class], modules = [ResultModule::class, DelegateModule::class])
interface ResultComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(view: WeakProperty<out Fragment>): Builder
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(params: ResultPresenterParams): Builder
        @BindsInstance
        fun bind(mode: ActivityMode): Builder
        fun dependencies(dependencies: ResultDependencies): Builder
        fun build(): ResultComponent
    }

    fun inject(target: ResultViewModel)
}

@Module
class ResultModule {

    @Provides
    @ResultScope
    fun activity(property: WeakProperty<out Fragment>): ActivityProperty {
        return property.map { it?.activity }
    }
}

interface ResultDependencies : DelegateModule.Dependencies {
    fun appResources(): AppResources
    fun finderStore(): FinderStore
    fun finderService(): FinderService
    fun resultStore(): ResultStore
    fun resultChannel(): ResultChannel
    fun curtainChannel(): CurtainChannel
    fun workManager(): WorkManager
    fun apks(): ApkDelegate
    fun utils(): UtilService
    fun explorerService(): ExplorerService
}
