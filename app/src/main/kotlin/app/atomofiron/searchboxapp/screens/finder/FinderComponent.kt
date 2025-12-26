package app.atomofiron.searchboxapp.screens.finder

import android.content.Context
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.Registerable
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.di.dependencies.channel.PreferenceChannel
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao
import app.atomofiron.searchboxapp.di.dependencies.service.FinderService
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.common.delegates.StoragePermissionDelegate
import app.atomofiron.searchboxapp.screens.finder.di.history.HistoryDao
import app.atomofiron.searchboxapp.screens.finder.di.history.HistoryDatabase
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class FinderScope

@FinderScope
@Component(dependencies = [FinderDependencies::class], modules = [FinderModule::class])
interface FinderComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(view: WeakProperty<out Fragment>): Builder
        fun dependencies(dependencies: FinderDependencies): Builder
        fun build(): FinderComponent
    }

    fun inject(target: FinderViewModel)
}

@Module
class FinderModule {

    @Provides
    @FinderScope
    fun storagePermissionDelegate(fragment: WeakProperty<out Fragment>): StoragePermissionDelegate {
        return StoragePermissionDelegate(fragment)
    }

    @Provides
    @FinderScope
    fun registerable(
        router: FinderRouter,
        storagePermissionDelegate: StoragePermissionDelegate,
    ) = Registerable(router, storagePermissionDelegate)

    @Provides
    @FinderScope
    fun dao(context: Context): HistoryDao = HistoryDatabase(context).historyDao()
}

interface FinderDependencies {
    fun context(): Context
    fun preferenceChannel(): PreferenceChannel
    fun explorerStore(): ExplorerStore
    fun preferenceStore(): PreferenceStore
    fun finderService(): FinderService
    fun finderStore(): FinderStore
    fun finderDao(): FinderDao
}
