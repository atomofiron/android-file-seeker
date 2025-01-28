package app.atomofiron.searchboxapp.screens.preferences.fragment

import android.content.res.Resources
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import androidx.preference.*
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.preference.TextFieldPreference
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewState
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys

class PreferenceFragmentDelegate(
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

        when (preference.key) {
            PreferenceKeys.KeySpecialCharacters.name -> {
                preference as TextFieldPreference
                preference.setFilter {
                    it.replace(Regex("[ ]+"), Const.SPACE).trim()
                }
            }
        }
    }

    private fun updatePreference(preference: Preference, newValue: Any? = null): Boolean {
        when (val key = preference.key) {
            PreferenceKeys.KeySpecialCharacters.name -> preference.updateStringSummary(newValue as String?)
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
            PreferenceKeys.KeyToybox.name -> preference.isVisible = SDK_INT < Q
            PreferenceKeys.PREF_CATEGORY_SYSTEM -> preference.isVisible = SDK_INT < Q
        }
        return true
    }

    private fun Preference.updateStringSummary(newValue: String? = null) {
        summary = newValue ?: preferenceDataStore?.getString(key, null)
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            PreferenceKeys.PREF_EXPORT_IMPORT -> clickOutput.onExportImportClick()
            PreferenceKeys.KeyExplorerItem.name -> clickOutput.onExplorerItemClick()
            PreferenceKeys.KeyJoystick.name -> clickOutput.onJoystickClick()
            PreferenceKeys.KeyToybox.name -> clickOutput.onToyboxClick()
        }
        return true
    }
}