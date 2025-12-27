package app.atomofiron.searchboxapp.screens.preferences

import android.content.Context
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceDataStore
import app.atomofiron.common.util.ActivityProperty
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.custom.preference.UpdateActionListener
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.channel.PreferenceChannel
import app.atomofiron.searchboxapp.di.dependencies.service.AppUpdateService
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.di.dependencies.store.AppUpdateStore
import app.atomofiron.searchboxapp.di.module.DelegateModule
import app.atomofiron.searchboxapp.model.AppSource
import app.atomofiron.searchboxapp.screens.preferences.fragment.LegacyPreferenceDataStore
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceClickOutput
import app.atomofiron.searchboxapp.screens.preferences.presenter.ExportImportPresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.PreferenceClickPresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.UpdatePresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExportImportDelegate
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import debug.LeakWatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope

@Scope
@MustBeDocumented
@Retention
annotation class PreferenceScope

@PreferenceScope
@Component(dependencies = [PreferenceDependencies::class], modules = [PreferenceModule::class, DelegateModule::class])
interface PreferenceComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun bind(scope: CoroutineScope): Builder
        @BindsInstance
        fun bind(view: WeakProperty<out Fragment>): Builder
        fun dependencies(dependencies: PreferenceDependencies): Builder
        fun build(): PreferenceComponent
    }

    fun inject(target: PreferenceViewModel)
}

@Module
abstract class PreferenceModule {

    @Binds
    @PreferenceScope
    abstract fun exportImportPresenterDelegate(impl: ExportImportPresenterDelegate): ExportImportDelegate.ExportImportOutput

    @Binds
    @PreferenceScope
    abstract fun preferenceClickOutput(impl: PreferenceClickPresenterDelegate): PreferenceClickOutput

    @Binds
    @PreferenceScope
    abstract fun updatePresenterDelegate(impl: UpdatePresenterDelegate): UpdateActionListener

    @Binds
    @PreferenceScope
    abstract fun preferenceDataStore(impl: LegacyPreferenceDataStore): PreferenceDataStore

    companion object {

        @Provides
        @PreferenceScope
        fun activity(property: WeakProperty<out Fragment>): ActivityProperty = property.map { it?.activity }
    }
}

interface PreferenceDependencies : DelegateModule.Dependencies {
    fun appSource(): AppSource
    fun appResources(): AppResources
    fun preferenceChannel(): PreferenceChannel
    fun context(): Context
    fun curtainChannel(): CurtainChannel
    fun appWatcherProxy(): LeakWatcher
    fun updateStore(): AppUpdateStore
    fun appUpdateService(): AppUpdateService
    fun packageManager(): PackageManager
}
