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
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import debug.LeakWatcher

class LegacyPreferenceDataStore(
    private val preferenceStore: PreferenceStore,
    private val scope: CoroutineScope,
    private val watcher: LeakWatcher,
) : PreferenceDataStore(), DataStore<Preferences> by preferenceStore {

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return when (key) {
            PreferenceKeys.PREF_LEAK_CANARY -> watcher.isEnabled
            PreferenceKeys.KeyToybox.name -> preferenceStore.getOrDefault(PreferenceKeys.KeyToybox.key)
                .let { ToyboxVariant(it) is ToyboxVariant.Embedded }
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
            PreferenceKeys.KeyToybox.name -> putString(PreferenceKeys.KeyToybox.name, ToyboxVariant(value).path)
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