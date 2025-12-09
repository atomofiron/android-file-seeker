package app.atomofiron.searchboxapp.utils.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import app.atomofiron.common.util.StringSet

@ConsistentCopyVisibility
data class PreferenceKey<T : Any> private constructor(
    val key: Preferences.Key<T>,
    val default: T,
    private val resetValue: T?,
) {
    companion object {
        operator fun invoke(name: String, default: Boolean) = PreferenceKey(booleanPreferencesKey(name), default, null)
        operator fun invoke(name: String, default: Int) = PreferenceKey(intPreferencesKey(name), default, null)
        operator fun invoke(name: String, default: Long) = PreferenceKey(longPreferencesKey(name), default, null)
        operator fun invoke(name: String, default: String, resetValue: String? = null) = PreferenceKey(stringPreferencesKey(name), default, resetValue)
        operator fun invoke(name: String, default: StringSet, resetValue: StringSet? = null) = PreferenceKey(stringSetPreferencesKey(name), default, resetValue)
        operator fun invoke(name: String, default: ByteArray, resetValue: ByteArray? = null) = PreferenceKey(byteArrayPreferencesKey(name), default, resetValue)
    }

    val name = key.name

    fun check(new: T): T = when (resetValue) {
        null -> new
        new -> default
        else -> new
    }
}
