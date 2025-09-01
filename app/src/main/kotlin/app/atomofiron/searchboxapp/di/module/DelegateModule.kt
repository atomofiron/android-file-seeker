package app.atomofiron.searchboxapp.di.module

import app.atomofiron.common.util.dialog.ActivityProperty
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.common.util.dialog.DialogDelegateImpl
import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.service.UtilService
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.common.delegates.FileOperationsDelegate
import dagger.Module
import dagger.Provides

@Module
open class DelegateModule {

    @Provides
    open fun provideDialogDelegate(activityProperty: ActivityProperty): DialogDelegate = DialogDelegateImpl(activityProperty)

    @Provides
    open fun provideFileOperationsDelegate(
        preferenceStore: PreferenceStore,
        apks: ApkInteractor,
        dialogs: DialogDelegate,
        utils: UtilService,
    ): FileOperationsDelegate {
        return FileOperationsDelegate(preferenceStore, apks, dialogs, utils)
    }
}
