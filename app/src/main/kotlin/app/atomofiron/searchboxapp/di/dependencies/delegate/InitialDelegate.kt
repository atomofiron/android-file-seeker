package app.atomofiron.searchboxapp.di.dependencies.delegate

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import app.atomofiron.common.util.extension.withMain
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.android.NativeBridge
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.utils.ExplorerUtils
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyAppTheme
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyDeepBlack
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeySuCmd
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitialDelegate @Inject constructor(
    context: Context,
    packageManager: PackageManager,
    preferences: PreferenceStore,
    scope: AppScope,
) {
    companion object {
        private const val PRIVATE_PREFERENCES_NAME = "initial_preferences"
    }

    private val sp = context.getSharedPreferences(PRIVATE_PREFERENCES_NAME, Application.MODE_PRIVATE)
    private val filesDirPath = context.filesDir.absolutePath

    init {
        ExplorerUtils.packageManager.value = packageManager
        NativeBridge.setSuCmd(getSuCmd(), filesDirPath)
        preferences.appTheme.collect(scope, ::updateTheme)
        preferences.suCmd.collect(scope, ::updateSuCmd)
    }

    fun getTheme(): AppTheme {
        val themeName = sp.getString(KeyAppTheme.name, AppTheme.defaultName())
        val deepBlack = sp.getBoolean(KeyDeepBlack.name, false)
        return AppTheme.fromString(themeName, deepBlack)
    }

    suspend fun updateTheme(appTheme: AppTheme) {
        sp.edit {
            putString(KeyAppTheme.name, appTheme.name)
            putBoolean(KeyDeepBlack.name, appTheme.deepBlack)
        }
        withMain(now = true) {
            applyTheme()
        }
    }

    fun getSuCmd(): String = sp.getString(KeySuCmd.name, null) ?: KeySuCmd.default

    fun updateSuCmd(cmd: String) {
        sp.edit { putString(KeySuCmd.name, cmd) }
        NativeBridge.setSuCmd(cmd, filesDirPath)
    }

    fun applyTheme() {
        val mode = when (getTheme()) {
            is AppTheme.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            is AppTheme.Light -> AppCompatDelegate.MODE_NIGHT_NO
            is AppTheme.Dark -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}