package app.atomofiron.searchboxapp.screens.explorer

import android.content.Context
import android.content.res.AssetManager
import androidx.fragment.app.Fragment
import androidx.work.WorkManager
import app.atomofiron.common.arch.Registerable
import app.atomofiron.common.util.ActivityProperty
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.dependencies.channel.CommonChannel
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.channel.PreferenceChannel
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.module.DelegateModule
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.common.delegates.StoragePermissionDelegate
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class ExplorerScope

@ExplorerScope
@Component(dependencies = [ExplorerDependencies::class], modules = [ExplorerModule::class, DelegateModule::class])
interface ExplorerComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(view: WeakProperty<out Fragment>): Builder
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(mode: ActivityMode): Builder
        fun dependencies(dependencies: ExplorerDependencies): Builder
        fun build(): ExplorerComponent
    }

    fun inject(target: ExplorerViewModel)
}

@Module
class ExplorerModule {

    @Provides
    @ExplorerScope
    fun storagePermissionDelegate(property: WeakProperty<out Fragment>): StoragePermissionDelegate {
        return StoragePermissionDelegate(property)
    }

    @Provides
    @ExplorerScope
    fun activity(property: WeakProperty<out Fragment>): ActivityProperty {
        return property.map { it?.activity }
    }

    @Provides
    @ExplorerScope
    fun registerable(
        router: ExplorerRouter,
        storagePermissionDelegate: StoragePermissionDelegate,
    ) = Registerable(router, storagePermissionDelegate)
}

interface ExplorerDependencies : DelegateModule.Dependencies {
    fun context(): Context
    fun assetManager(): AssetManager
    fun explorerStore(): ExplorerStore
    fun curtainChannel(): CurtainChannel
    fun mainChannel(): CommonChannel
    fun preferenceChannel(): PreferenceChannel
    fun workManager(): WorkManager
}
