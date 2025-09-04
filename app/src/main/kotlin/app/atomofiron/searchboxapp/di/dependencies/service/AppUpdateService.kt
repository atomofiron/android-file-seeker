package app.atomofiron.searchboxapp.di.dependencies.service

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.channel.PreferenceChannel
import app.atomofiron.searchboxapp.di.dependencies.store.AppUpdateStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.other.UpdateType

interface AppUpdateService {
    fun onActivityCreate(activity: AppCompatActivity)
    fun check(userAction: Boolean = false)
    fun retry()
    fun startUpdate(variant: UpdateType.Variant)
    fun completeUpdate()

    interface Factory {
        fun new(
            context: Context,
            scope: AppScope,
            apkService: ApkService,
            updateStore: AppUpdateStore,
            preferences: PreferenceStore,
            preferenceChannel: PreferenceChannel,
        ): AppUpdateService
    }
}
