package app.atomofiron.searchboxapp.di.dependencies.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import app.atomofiron.common.util.extension.launchOnMain
import app.atomofiron.common.util.flow.StateFlowProperty
import app.atomofiron.common.util.flow.asProperty
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.model.other.ByteSize
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.model.finder.SearchOptionsImpl
import app.atomofiron.searchboxapp.model.other.toByteSize
import app.atomofiron.searchboxapp.model.finder.toInt
import app.atomofiron.searchboxapp.model.preference.AppLocale
import app.atomofiron.searchboxapp.model.preference.AppOrientation
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.model.textviewer.LocalSearchOptions
import app.atomofiron.searchboxapp.screens.main.model.EasterEgg
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKey
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyAppOrientation
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyAppTheme
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyAppUpdateCode
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyClown
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyDeepBlack
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyDrawerGravity
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyExplorerItem
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyHalloween
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyHapticFeedback
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyJoystick
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyLocalSearchOptions
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyLocale
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyMaxDepth
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyMaxSize
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyNewYear
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyScreenshotOperations
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeySearchOptions
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyShowSearchOptions
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyShownNotificationUpdateCode
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeySpecialCharacters
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeySuCmd
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyTestField
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyUseSu
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyFolderVolumeUp
import app.atomofiron.searchboxapp.utils.preferences.get
import app.atomofiron.searchboxapp.utils.preferences.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "preferences",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, context.packageName + "_preferences"))
    },
)

@Singleton
class PreferenceStore @Inject constructor(
    private val context: Context,
    private val scope: AppScope,
) : DataStore<Preferences> by context.dataStore {

    private lateinit var preferences: Preferences

    init {
        scope.launch {
            data.collect {
                preferences = it
            }
        }
    }

    val asSu = getFlow(KeyUseSu)
    val suCmd = getFlow(KeySuCmd)
    val specialCharacters = getFlow(KeySpecialCharacters, ::readCharacters)
    val appOrientation = getFlow(KeyAppOrientation) { AppOrientation.entries[it.toInt()] }
    val explorerItemComposition = getFlow(KeyExplorerItem) { ExplorerItemComposition(it) }
    val joystickComposition = getFlow(KeyJoystick) { JoystickComposition(it) }
    var drawerGravity = getFlow(KeyDrawerGravity)
    val testField = getNullableFlow(KeyTestField)
    val showSearchOptions = getFlow(KeyShowSearchOptions)
    val searchOptions = getFlow(KeySearchOptions, ::SearchOptionsImpl)
    val localSearchOptions = getFlow(KeyLocalSearchOptions, ::LocalSearchOptions)
    val maxFileSizeForSearch = getFlow(KeyMaxSize) { it.toByteSize() }
    val appUpdateCode = getFlow(KeyAppUpdateCode)
    val shownNotificationUpdateCode = getFlow(KeyShownNotificationUpdateCode)
    val maxDepthForSearch = getFlow(KeyMaxDepth)
    val hapticFeedback = getFlow(KeyHapticFeedback)
    val folderVolumeUp = getFlow(KeyFolderVolumeUp)
    val screenshotOperations = getFlow(KeyScreenshotOperations)
    val eggHalloween = getFlow(KeyHalloween)
    val eggNewYear = getFlow(KeyNewYear)
    val eggClown = getFlow(KeyClown)
    val appLocale = getFlow(KeyLocale) { AppLocale.entries[it.toInt()] } // don't pass any default value
    val appTheme = data.map {
        val appThemeMode = it[KeyAppTheme] ?: AppTheme.defaultName()
        val deepBlack = it[KeyDeepBlack] ?: false
        AppTheme.fromString(appThemeMode, deepBlack)
    }.stateInProperty(scope, initial = null) // don't pass any default value

    operator fun invoke(block: suspend PreferenceStore.() -> Unit) {
        scope.launchOnMain(immediate = true, CoroutineStart.UNDISPATCHED) {
            this@PreferenceStore.block()
        }
    }

    suspend fun setUseSu(value: Boolean) {
        edit { it[KeyUseSu] = value }
    }

    suspend fun setDrawerGravity(value: Int) {
        edit { it[KeyDrawerGravity] = value }
    }

    suspend fun setTestField(value: String?) {
        edit { it[KeyTestField] = value }
    }

    suspend fun setShowSearchOptions(value: Boolean) {
        edit { it[KeyShowSearchOptions] = value }
    }

    suspend fun setSearchOptions(value: SearchOptions) {
        edit { it[KeySearchOptions] = value.toInt() }
    }

    suspend fun setLocalSearchOptions(value: LocalSearchOptions) {
        edit { it[KeyLocalSearchOptions] = value.toInt() }
    }

    suspend fun setSpecialCharacters(value: Array<String>) {
        edit { it[KeySpecialCharacters] = value.joinToString(separator = " ") }
    }

    suspend fun setMaxFileSizeForSearch(value: ByteSize) {
        edit { it[KeyMaxSize] = value.persistable() }
    }

    suspend fun setAppUpdateCode(value: Int) {
        edit { it[KeyAppUpdateCode] = value }
    }

    suspend fun setShownNotificationUpdateCode(value: Int) {
        edit { it[KeyShownNotificationUpdateCode] = value }
    }

    suspend fun setMaxDepthForSearch(value: Int) {
        edit { it[KeyMaxDepth] = value }
    }

    suspend fun setAppLocale(value: AppLocale) {
        edit { it[KeyLocale] = value.ordinal.toString() }
    }

    suspend fun setExplorerItemComposition(value: ExplorerItemComposition) {
        edit { it[KeyExplorerItem] = value.flags }
    }

    suspend fun setJoystickComposition(value: JoystickComposition) {
        edit { it[KeyJoystick] = value.data }
    }

    suspend fun setHapticFeedback(value: Boolean) {
        edit { it[KeyHapticFeedback] = value }
    }

    suspend fun setEasterEggEnabled(egg: EasterEgg, value: Boolean) {
        edit {
            val key = when (egg) {
                EasterEgg.Halloween -> KeyHalloween
                EasterEgg.NewYear -> KeyNewYear
                EasterEgg.Clown -> KeyClown
            }
            it[key] = value
        }
    }

    private fun readCharacters(value: String): Array<String> {
        return value.split(" ")
            .filter { it.isNotBlank() }
            .toTypedArray()
    }

    private fun <V : Any> getFlow(key: PreferenceKey<V>): StateFlowProperty<V> {
        return data.mapNotNull { it[key] ?: key.default }
            .stateInProperty(scope, initial = key.default)
    }

    private fun <V : Any> getNullableFlow(key: PreferenceKey<V>): StateFlowProperty<V?> {
        return data.map { it[key] }
            .stateInProperty(scope, initial = null)
    }

    private fun <V : Any, E> getFlow(key: PreferenceKey<V>, transformation: (V) -> E): StateFlowProperty<E> {
        return data.mapNotNull { (it[key] ?: key.default)
            .let(transformation) }
            .stateInProperty(scope, initial = transformation(key.default))
    }

    private fun <T> Flow<T>.stateInProperty(
        scope: CoroutineScope,
        initial: T?,
    ): StateFlowProperty<T> = distinctUntilChanged()
        .shareIn(scope, SharingStarted.Eagerly, replay = 1)
        .asProperty(initial)

    operator fun <T> get(key: Preferences.Key<T>): T = preferences[key] ?: PreferenceKeys.default(key.name)

    operator fun <T : Any> get(key: PreferenceKey<T>): T = preferences[key] ?: key.default
}
