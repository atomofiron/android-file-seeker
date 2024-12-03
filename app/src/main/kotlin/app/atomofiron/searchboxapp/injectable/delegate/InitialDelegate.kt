package app.atomofiron.searchboxapp.injectable.delegate

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyAppTheme
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyDeepBlack

class InitialDelegate(context: Context) {
    companion object {
        private const val PRIVATE_PREFERENCES_NAME = "initial_preferences"
    }

    private val sp = context.getSharedPreferences(PRIVATE_PREFERENCES_NAME, Application.MODE_PRIVATE)

    fun getTheme(): AppTheme {
        val themeName = sp.getString(KeyAppTheme.name, AppTheme.defaultName())
        val deepBlack = sp.getBoolean(KeyDeepBlack.name, false)
        return AppTheme.fromString(themeName, deepBlack)
    }

    fun updateTheme(appTheme: AppTheme) {
        sp.edit()
            .putString(KeyAppTheme.name, appTheme.name)
            .putBoolean(KeyDeepBlack.name, appTheme.deepBlack)
            .apply()
        applyTheme()
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