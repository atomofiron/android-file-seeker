package app.atomofiron.searchboxapp.screens.preferences.fragment

import android.content.res.Resources
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import androidx.preference.get
import app.atomofiron.common.util.Android
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.preference.AppLocale
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewState
import app.atomofiron.searchboxapp.utils.performHapticLite
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys
import app.atomofiron.searchboxapp.utils.setHapticEffect

class PreferenceFragmentDelegate(
    private val view: () -> View?,
    private val resources: Resources,
    private val viewState: PreferenceViewState,
    private val clickOutput: PreferenceClickOutput,
) : Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        return updatePreference(preference, newValue)
    }

    fun onCreatePreference(preference: Preference) {
        setPreferenceListeners(preference)
        updatePreference(preference)
        if (preference is PreferenceGroup) {
            for (i in 0 until preference.preferenceCount) {
                onCreatePreference(preference[i])
            }
        }
    }

    private fun setPreferenceListeners(preference: Preference) {
        preference.onPreferenceChangeListener = this
        preference.onPreferenceClickListener = this
    }

    private fun updatePreference(preference: Preference, newValue: Any? = null): Boolean {
        when (val key = preference.key) {
            PreferenceKeys.KeyUseSu.name -> newValue?.let {
                return clickOutput.onUseSuChanged(newValue as Boolean)
            }
            PreferenceKeys.KeyHapticFeedback.name -> newValue?.let {
                view()?.setHapticEffect(newValue as Boolean)
            }
            PreferenceKeys.KeyAppTheme.name -> {
                var name = newValue?.toString() ?: preference.preferenceDataStore?.getString(key, null)
                name = AppTheme.fromString(name).name
                val index = resources.getStringArray(R.array.theme_val).indexOf(name)
                preference.summary = resources.getStringArray(R.array.theme_var)[index]
            }
            PreferenceKeys.KeyAppOrientation.name -> {
                val i = (newValue?.toString() ?: preference.preferenceDataStore?.getString(key, null))?.toInt() ?: 0
                preference.summary = resources.getStringArray(R.array.orientation_var)[i]
            }
            PreferenceKeys.PREF_EXPORT_IMPORT -> preference.isEnabled = viewState.isExportImportAvailable
            PreferenceKeys.PREF_CATEGORY_SYSTEM -> preference.isVisible = Android.Below.Q
            PreferenceKeys.KeyLocale.name -> (newValue as String?)?.toInt()?.let { index ->
                view()?.post { // let persist the new value
                    val locales = LocaleListCompat.forLanguageTags(AppLocale.entries[index].tag)
                    AppCompatDelegate.setApplicationLocales(locales)
                }
            }
        }
        return true
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        view()?.performHapticLite()
        when (preference.key) {
            PreferenceKeys.PREF_EXPORT_IMPORT -> clickOutput.onExportImportClick()
            PreferenceKeys.PREF_COLOR_SCHEME -> clickOutput.onColorSchemeClick()
            PreferenceKeys.KeyExplorerItem.name -> clickOutput.onExplorerItemClick()
            PreferenceKeys.KeyJoystick.name -> clickOutput.onJoystickClick()
            PreferenceKeys.KeyLocale.name -> clickOutput.onLocaleClick()
        }
        return true
    }
}