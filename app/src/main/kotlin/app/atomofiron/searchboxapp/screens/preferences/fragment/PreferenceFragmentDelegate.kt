package app.atomofiron.searchboxapp.screens.preferences.fragment

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import androidx.preference.get
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.preference.ChipPreference
import app.atomofiron.searchboxapp.model.preference.AppLocale
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.common.delegates.StoragePermissionDelegate
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewState
import app.atomofiron.searchboxapp.utils.performHapticLite
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyAppOrientation
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyAppTheme
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyExplorerItem
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyHapticFeedback
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyJoystick
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyLocale
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyScreenshotOperations
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyUseSu
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.PREF_COLOR_SCHEME
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.PREF_EXPORT_IMPORT
import app.atomofiron.searchboxapp.utils.setHapticEffect

class PreferenceFragmentDelegate(
    private val fragment: Fragment,
    private val resources: Resources,
    private val viewState: PreferenceViewState,
    private val clickOutput: PreferenceClickOutput,
) : Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private val view get() = fragment.view
    private val permissions = StoragePermissionDelegate(WeakProperty(fragment))
        .apply { register() }

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
        preference.onPreferenceClickListener = when {
            preference !is ChipPreference -> this
            preference.key != KeyLocale.name -> null
            Android.T -> this
            else -> null
        }
    }

    private fun updatePreference(preference: Preference, newValue: Any? = null): Boolean {
        when (val key = preference.key) {
            KeyUseSu.name -> newValue?.let {
                return clickOutput.onUseSuChanged(newValue as Boolean)
            }
            KeyHapticFeedback.name -> newValue?.let {
                view?.setHapticEffect(newValue as Boolean)
            }
            KeyAppTheme.name -> {
                var name = newValue?.toString() ?: preference.preferenceDataStore?.getString(key, null)
                name = AppTheme.fromString(name).name
                val index = resources.getStringArray(R.array.theme_val).indexOf(name)
                preference.summary = resources.getStringArray(R.array.theme_var)[index]
            }
            KeyAppOrientation.name -> {
                val i = (newValue?.toString() ?: preference.preferenceDataStore?.getString(key, null))?.toInt() ?: 0
                preference.summary = resources.getStringArray(R.array.orientation_var)[i]
            }
            KeyScreenshotOperations.name -> {
                if (newValue == true) requestScreenshotOperationPermissions()
            }
            PREF_EXPORT_IMPORT -> preference.isEnabled = viewState.isExportImportAvailable
            KeyLocale.name -> (newValue as String?)?.toInt()?.let { index ->
                view?.post { // let persist the new value
                    val locales = LocaleListCompat.forLanguageTags(AppLocale.entries[index].tag)
                    AppCompatDelegate.setApplicationLocales(locales)
                }
            }
        }
        return true
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            PREF_EXPORT_IMPORT -> clickOutput.onExportImportClick()
            PREF_COLOR_SCHEME -> clickOutput.onColorSchemeClick()
            KeyExplorerItem.name -> clickOutput.onExplorerItemClick()
            KeyJoystick.name -> clickOutput.onJoystickClick()
            KeyLocale.name -> clickOutput.onLocaleClick()
            else -> return false
        }
        view?.performHapticLite()
        return true
    }

    private fun requestScreenshotOperationPermissions() {
        permissions.request(
            granted = {
                permissions { request(POST_NOTIFICATIONS) }
            },
            denied = { },
        )
    }
}