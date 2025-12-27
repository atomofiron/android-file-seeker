package app.atomofiron.searchboxapp.screens.preferences.fragment

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.preference.PreferenceDataStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.preferences.PreferenceScope
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys
import debug.LeakWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@PreferenceScope
class LegacyPreferenceDataStore @Inject constructor(
    private val store: PreferenceStore,
    private val scope: CoroutineScope,
    private val watcher: LeakWatcher,
) : PreferenceDataStore(), DataStore<Preferences> by store {

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return when (key) {
            PreferenceKeys.PREF_LEAK_CANARY -> watcher.isEnabled
            else -> store[booleanPreferencesKey(key)]
        }
    }

    override fun getInt(key: String, defValue: Int): Int {
        return store[intPreferencesKey(key)]
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return store[floatPreferencesKey(key)]
    }

    override fun getLong(key: String, defValue: Long): Long {
        return store[longPreferencesKey(key)]
    }

    override fun getString(key: String, defValue: String?): String? {
        return store[stringPreferencesKey(key)]
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return store[stringSetPreferencesKey(key)]
    }

    override fun putBoolean(key: String, value: Boolean) {
        when (key) {
            PreferenceKeys.PREF_LEAK_CANARY -> watcher.isEnabled = value
            else -> launchImmediately {
                val pKey = booleanPreferencesKey(key)
                edit { it[pKey] = PreferenceKeys[pKey].check(value) }
            }
        }
    }

    override fun putInt(key: String, value: Int) {
        launchImmediately {
            val pKey = intPreferencesKey(key)
            edit {
                it[pKey] = PreferenceKeys[pKey].check(value)
            }
        }
    }

    override fun putFloat(key: String, value: Float) {
        launchImmediately {
            val pKey = floatPreferencesKey(key)
            edit {
                it[pKey] = PreferenceKeys[pKey].check(value)
            }
        }
    }

    override fun putLong(key: String, value: Long) {
        launchImmediately {
            val pKey = longPreferencesKey(key)
            edit {
                it[pKey] = PreferenceKeys[pKey].check(value)
            }
        }
    }

    override fun putString(key: String, value: String?) {
        val pKey = stringPreferencesKey(key)
        launchImmediately {
            edit {
                when (value) {
                    null -> it.remove(pKey)
                    else -> it[pKey] = PreferenceKeys[pKey].check(value)
                }
            }
        }
    }

    override fun putStringSet(key: String, values: Set<String>?) {
        val pKey = stringSetPreferencesKey(key)
        launchImmediately {
            edit {
                when (values) {
                    null -> it.remove(pKey)
                    else -> it[pKey] = PreferenceKeys[pKey].check(values)
                }
            }
        }
    }

    private fun launchImmediately(block: suspend CoroutineScope.() -> Unit) {
        scope.launch(Dispatchers.Main.immediate, start = CoroutineStart.UNDISPATCHED, block)
    }
}