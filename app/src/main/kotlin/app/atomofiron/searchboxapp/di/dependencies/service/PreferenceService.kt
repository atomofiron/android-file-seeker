package app.atomofiron.searchboxapp.di.dependencies.service

import android.content.Context
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.utils.Rslt

class PreferenceService(
    val context: Context,
    val preferenceStore: PreferenceStore,
) {
    private val packageName = context.packageName
    private val internalPath = context.applicationInfo.dataDir

    private val prefs = "$internalPath/shared_prefs/${packageName}_preferences.xml"
    private val history = "$internalPath/databases/history*"

    fun exportPreferences(): Rslt<Unit> = Rslt.Ok

    fun exportHistory(): Rslt<Unit> = Rslt.Ok

    fun importPreferences(): Rslt<Unit> = Rslt.Ok

    fun importHistory(): Rslt<Unit> = Rslt.Ok
}