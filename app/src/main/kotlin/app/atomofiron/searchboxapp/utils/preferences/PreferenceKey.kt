package app.atomofiron.searchboxapp.utils.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

data class PreferenceKey<T> private constructor(
    val key: Preferences.Key<T>,
    val default: T,
) {
    companion object {
        operator fun invoke(name: String, default: Boolean) = PreferenceKey(booleanPreferencesKey(name), default)
        operator fun invoke(name: String, default: Int) = PreferenceKey(intPreferencesKey(name), default)
        operator fun invoke(name: String, default: String) = PreferenceKey(stringPreferencesKey(name), default)
        operator fun invoke(name: String, default: Set<String>) = PreferenceKey(stringSetPreferencesKey(name), default)
    }
    val name = key.name
}
