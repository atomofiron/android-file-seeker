package app.atomofiron.searchboxapp.injectable.service

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.store.AppUpdateStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.other.UpdateType
import kotlinx.coroutines.CoroutineScope

interface AppUpdateService {
    fun onActivityCreate(activity: AppCompatActivity)
    fun check(userAction: Boolean = false)
    fun retry()
    fun startUpdate(variant: UpdateType.Variant)
    fun completeUpdate()

    interface Factory {
        fun new(
            context: Context,
            scope: CoroutineScope,
            apkService: ApkService,
            updateStore: AppUpdateStore,
            preferences: PreferenceStore,
            preferenceChannel: PreferenceChannel,
        ): AppUpdateService
    }
}
