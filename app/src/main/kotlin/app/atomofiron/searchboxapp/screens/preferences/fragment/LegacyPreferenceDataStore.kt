package app.atomofiron.searchboxapp.screens.preferences.fragment

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.preference.PreferenceDataStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.utils.AppWatcherProxy
import app.atomofiron.searchboxapp.utils.PreferenceKeys
import kotlinx.coroutines.*

class LegacyPreferenceDataStore(
    private val preferenceStore: PreferenceStore,
    private val scope: CoroutineScope,
    private val watcher: AppWatcherProxy,
) : PreferenceDataStore(), DataStore<Preferences> by preferenceStore {

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return when (key) {
            PreferenceKeys.PREF_LEAK_CANARY -> watcher.isEnabled
            else -> preferenceStore.getOrDefault(booleanPreferencesKey(key))
        }
    }

    override fun getInt(key: String, defValue: Int): Int {
        return preferenceStore.getOrDefault(intPreferencesKey(key))
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return preferenceStore.getOrDefault(floatPreferencesKey(key))
    }

    override fun getLong(key: String, defValue: Long): Long {
        return preferenceStore.getOrDefault(longPreferencesKey(key))
    }

    override fun getString(key: String, defValue: String?): String? {
        return preferenceStore.getOrDefault(stringPreferencesKey(key))
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return preferenceStore.getOrDefault(stringSetPreferencesKey(key))
    }

    override fun putBoolean(key: String, value: Boolean) {
        when (key) {
            PreferenceKeys.PREF_LEAK_CANARY -> watcher.isEnabled = value
            else -> launchImmediately {
                edit {
                    it[booleanPreferencesKey(key)] = value
                }
            }
        }
    }

    override fun putInt(key: String, value: Int) {
        launchImmediately {
            edit {
                it[intPreferencesKey(key)] = value
            }
        }
    }

    override fun putFloat(key: String, value: Float) {
        launchImmediately {
            edit {
                it[floatPreferencesKey(key)] = value
            }
        }
    }

    override fun putLong(key: String, value: Long) {
        launchImmediately {
            edit {
                it[longPreferencesKey(key)] = value
            }
        }
    }

    override fun putString(key: String, value: String?) {
        val pKey = stringPreferencesKey(key)
        launchImmediately {
            edit {
                when (value) {
                    null -> it.remove(pKey)
                    else -> it[pKey] = value
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
                    else -> it[pKey] = values
                }
            }
        }
    }

    private fun launchImmediately(block: suspend CoroutineScope.() -> Unit) {
        scope.launch(Dispatchers.Main.immediate, start = CoroutineStart.UNDISPATCHED, block)
    }
}