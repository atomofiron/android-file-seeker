package app.atomofiron.searchboxapp.di.dependencies.service

import android.content.Context
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.utils.Shell

class PreferenceService(
    val context: Context,
    val preferenceStore: PreferenceStore,
) {
    private val packageName = context.packageName
    private val toybox: String get() = Shell.toyboxPath
    private val internalPath = context.applicationInfo.dataDir
    private val externalPath: String get() = context.getExternalFilesDir(null)!!.absolutePath

    fun exportPreferences(): Shell.Output {
        return Shell.exec("$toybox cp -f $internalPath/shared_prefs/${packageName}_preferences.xml $externalPath/", su = false)
    }

    fun exportHistory(): Shell.Output {
        return Shell.exec("$toybox cp -f $internalPath/databases/history* $externalPath/", su = false)
    }

    fun importPreferences(): Shell.Output {
        return Shell.exec("$toybox cp -f $externalPath/${packageName}_preferences.xml $internalPath/shared_prefs/", su = false)
    }

    fun importHistory(): Shell.Output {
        return Shell.exec("$toybox cp -f $externalPath/history* $internalPath/databases/", su = false)
    }
}