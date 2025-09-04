package app.atomofiron.searchboxapp.screens.preferences

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceDataStore
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.custom.preference.UpdateActionListener
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.channel.PreferenceChannel
import app.atomofiron.searchboxapp.di.dependencies.service.AppUpdateService
import app.atomofiron.searchboxapp.di.dependencies.service.PreferenceService
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.di.dependencies.store.AppUpdateStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.AppSource
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceClickOutput
import app.atomofiron.searchboxapp.screens.preferences.presenter.ExportImportPresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.PreferenceClickPresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.UpdatePresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExportImportDelegate
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
@Component(dependencies = [PreferenceDependencies::class], modules = [PreferenceModule::class])
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
class PreferenceModule {

    @Provides
    @PreferenceScope
    fun exportImportPresenterDelegate(
        scope: CoroutineScope,
        viewState: PreferenceViewState,
        preferenceService: PreferenceService,
        preferenceChannel: PreferenceChannel,
    ): ExportImportDelegate.ExportImportOutput {
        return ExportImportPresenterDelegate(scope, viewState, preferenceService, preferenceChannel)
    }

    @Provides
    @PreferenceScope
    fun preferenceClickPresenterDelegate(
        scope: CoroutineScope,
        viewState: PreferenceViewState,
        router: PreferenceRouter,
        exportImportDelegate: ExportImportDelegate.ExportImportOutput,
        preferenceStore: PreferenceStore,
        curtainChannel: CurtainChannel,
        resources: AppResources,
        appSource: AppSource,
    ): PreferenceClickOutput {
        return PreferenceClickPresenterDelegate(
            scope,
            viewState,
            router,
            exportImportDelegate,
            preferenceStore,
            curtainChannel,
            resources,
            appSource,
        )
    }

    @Provides
    @PreferenceScope
    fun updatePresenterDelegate(service: AppUpdateService): UpdateActionListener {
        return UpdatePresenterDelegate(service)
    }

    @Provides
    @PreferenceScope
    fun presenter(
        scope: CoroutineScope,
        viewState: PreferenceViewState,
        router: PreferenceRouter,
        exportImportDelegate: ExportImportDelegate.ExportImportOutput,
        preferenceClickOutput: PreferenceClickOutput,
        preferenceStore: PreferenceStore,
        updateDelegate: UpdateActionListener,
    ): PreferencePresenter {
        return PreferencePresenter(
            scope,
            viewState,
            router,
            exportImportDelegate,
            preferenceClickOutput,
            preferenceStore,
            updateDelegate,
        )
    }

    @Provides
    @PreferenceScope
    fun preferenceService(context: Context, preferenceStore: PreferenceStore): PreferenceService {
        return PreferenceService(context, preferenceStore)
    }

    @Provides
    @PreferenceScope
    fun router(fragment: WeakProperty<out Fragment>): PreferenceRouter = PreferenceRouter(fragment)

    @Provides
    @PreferenceScope
    fun viewState(
        scope: CoroutineScope,
        preferenceDataStore: PreferenceDataStore,
        updateStore: AppUpdateStore,
        preferenceStore: PreferenceStore,
        appWatcher: LeakWatcher,
        preferenceChannel: PreferenceChannel,
    ): PreferenceViewState = PreferenceViewState(scope, preferenceDataStore, preferenceStore, preferenceChannel, updateStore, appWatcher)
}

interface PreferenceDependencies {
    fun appSource(): AppSource
    fun appResources(): AppResources
    fun preferenceChannel(): PreferenceChannel
    fun preferenceStore(): PreferenceStore
    fun context(): Context
    fun curtainChannel(): CurtainChannel
    fun appWatcherProxy(): LeakWatcher
    fun updateStore(): AppUpdateStore
    fun appUpdateService(): AppUpdateService
    fun preferenceDataStore(): PreferenceDataStore
}
