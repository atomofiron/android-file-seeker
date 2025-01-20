package app.atomofiron.searchboxapp.injectable.service

import androidx.appcompat.app.AppCompatActivity
import app.atomofiron.searchboxapp.model.other.UpdateType

interface AppUpdateService {
    fun onActivityCreate(activity: AppCompatActivity)
    fun check(userAction: Boolean = false)
    fun retry()
    fun startUpdate(variant: UpdateType.Variant)
    fun completeUpdate()
}
