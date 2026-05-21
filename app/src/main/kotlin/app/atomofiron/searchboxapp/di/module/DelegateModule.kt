package app.atomofiron.searchboxapp.di.module

import app.atomofiron.common.util.ActivityProperty
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.common.util.dialog.DialogDelegateImpl
import app.atomofiron.searchboxapp.di.dependencies.router.FilePickingDelegate
import app.atomofiron.searchboxapp.di.dependencies.router.FileSharingDelegate
import app.atomofiron.searchboxapp.di.dependencies.router.FileSharingDelegateImpl
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import dagger.Module
import dagger.Provides

@Module
class DelegateModule {
    interface Dependencies {
        val preferences: PreferenceStore
    }

    @Provides
    fun provideDialogDelegate(activityProperty: ActivityProperty): DialogDelegate = DialogDelegateImpl(activityProperty)

    @Provides
    fun fileSharingDelegate(
        activityProperty: ActivityProperty,
        preferences: PreferenceStore,
    ): FileSharingDelegate = FileSharingDelegateImpl(activityProperty, preferences)

    @Provides
    fun filePickingDelegate(
        activityProperty: ActivityProperty,
        preferences: PreferenceStore,
    ): FilePickingDelegate = FileSharingDelegateImpl(activityProperty, preferences)
}
