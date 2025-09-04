package app.atomofiron.searchboxapp.utils.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences

operator fun <T> Preferences.get(key: PreferenceKey<T>) = get(key.key)

operator fun <T> MutablePreferences.set(key: PreferenceKey<T>, value: T) = set(key.key, value)

fun <T> MutablePreferences.remove(key: PreferenceKey<T>) = remove(key.key)
