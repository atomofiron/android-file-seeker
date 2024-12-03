package app.atomofiron.searchboxapp.screens.preferences

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceDataStore
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.service.PreferenceService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.preferences.fragment.LegacyPreferenceDataStore
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceClickOutput
import app.atomofiron.searchboxapp.screens.preferences.presenter.ExportImportPresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.PreferenceClickPresenterDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExportImportDelegate
import app.atomofiron.searchboxapp.utils.AppWatcherProxy
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
        router: PreferenceRouter,
        exportImportDelegate: ExportImportDelegate.ExportImportOutput,
        preferenceStore: PreferenceStore,
        curtainChannel: CurtainChannel,
    ): PreferenceClickOutput {
        return PreferenceClickPresenterDelegate(
            scope,
            router,
            exportImportDelegate,
            preferenceStore,
            curtainChannel,
        )
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
        appStore: AppStore,
    ): PreferencePresenter {
        return PreferencePresenter(
            scope,
            viewState,
            router,
            exportImportDelegate,
            preferenceClickOutput,
            preferenceStore,
            appStore,
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
    ): PreferenceViewState = PreferenceViewState(scope, preferenceDataStore)

    @Provides
    @PreferenceScope
    fun preferenceDataStore(
        preferences: PreferenceStore,
        appStore: AppStore,
        watcher: AppWatcherProxy,
    ): PreferenceDataStore {
        return LegacyPreferenceDataStore(preferences, appStore.scope, watcher)
    }
}

interface PreferenceDependencies {
    fun preferenceChannel(): PreferenceChannel
    fun preferenceStore(): PreferenceStore
    fun context(): Context
    fun curtainChannel(): CurtainChannel
    fun appWatcherProxy(): AppWatcherProxy
    fun appStore(): AppStore
}
