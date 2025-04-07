package app.atomofiron.searchboxapp.di.module

import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.interactor.DialogInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.delegates.FileOperationsDelegate
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class DelegateModule {

    @Provides
    @Singleton
    open fun provideFileOperationsDelegate(
        preferenceStore: PreferenceStore,
        apks: ApkInteractor,
        dialogs: DialogInteractor,
    ): FileOperationsDelegate {
        return FileOperationsDelegate(preferenceStore, apks, dialogs)
    }
}
