package app.atomofiron.searchboxapp.utils.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences

operator fun <T : Any> Preferences.get(key: PreferenceKey<T>) = get(key.key)

operator fun <T : Any> MutablePreferences.set(key: PreferenceKey<T>, value: T?) = when (value) {
    null -> remove(key.key)
    else -> set(key.key, value)
}
