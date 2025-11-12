package app.atomofiron.searchboxapp.utils.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.google.android.gms.common.internal.Objects

class PreferenceKey<T> private constructor(
    val key: Preferences.Key<T>,
    val default: T,
    val resetValue: T?,
) {
    companion object {
        operator fun invoke(name: String, default: Boolean, resetValue: Boolean? = null) = PreferenceKey(booleanPreferencesKey(name), default, resetValue)
        operator fun invoke(name: String, default: Int, resetValue: Int? = null) = PreferenceKey(intPreferencesKey(name), default, resetValue)
        operator fun invoke(name: String, default: Long, resetValue: Long? = null) = PreferenceKey(longPreferencesKey(name), default, resetValue)
        operator fun invoke(name: String, default: String, resetValue: String? = null) = PreferenceKey(stringPreferencesKey(name), default, resetValue)
        operator fun invoke(name: String, default: Set<String>, resetValue: Set<String>? = null) = PreferenceKey(stringSetPreferencesKey(name), default, resetValue)
    }
    val name = key.name

    override fun hashCode(): Int = Objects.hashCode(this::class, key)

    override fun equals(other: Any?): Boolean = when {
        other !is PreferenceKey<*> -> false
        else -> other.key == key
    }

    override fun toString(): String = "PreferenceKey(key=$key, default=$default, resetValue=$resetValue)"

    fun check(new: T): T = when (new) {
        resetValue -> default
        else -> new
    }
}
