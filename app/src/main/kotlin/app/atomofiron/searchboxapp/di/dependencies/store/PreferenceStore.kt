package app.atomofiron.searchboxapp.di.dependencies.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import app.atomofiron.common.util.flow.StateFlowProperty
import app.atomofiron.common.util.flow.asProperty
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.model.preference.*
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKey
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyAppOrientation
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyAppTheme
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyAppUpdateCode
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyDeepBlack
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyDrawerGravity
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyExplorerItem
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyHapticFeedback
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyJoystick
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyLocale
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyMaxDepth
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyMaxSize
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyOpenedDirPath
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeySearchOptions
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyShowSearchOptions
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeySpecialCharacters
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyToybox
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyUseSu
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyShownNotificationUpdateCode
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys.KeyTestField
import app.atomofiron.searchboxapp.utils.preferences.get
import app.atomofiron.searchboxapp.utils.preferences.remove
import app.atomofiron.searchboxapp.utils.preferences.set
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "preferences",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, context.packageName + "_preferences"))
    },
)

class PreferenceStore(
    private val context: Context,
    private val scope: CoroutineScope,
) : DataStore<Preferences> by context.dataStore {

    private lateinit var preferences: Preferences

    init {
        scope.launch {
            data.collect {
                preferences = it
            }
        }
    }

    operator fun invoke(block: suspend PreferenceStore.() -> Unit) {
        scope.launch(Dispatchers.Main.immediate, CoroutineStart.UNDISPATCHED) {
            this@PreferenceStore.block()
        }
    }

    val useSu = getFlow(KeyUseSu)

    suspend fun setUseSu(value: Boolean) {
        edit { it[KeyUseSu] = value }
    }

    val openedDirPath = getFlow(KeyOpenedDirPath)

    suspend fun setOpenedDirPath(value: String?) {
        edit {
            when (value) {
                null -> it.remove(KeyOpenedDirPath)
                else -> it[KeyOpenedDirPath] = value
            }
        }
    }

    var drawerGravity = getFlow(KeyDrawerGravity)

    suspend fun setDrawerGravity(value: Int) {
        edit { it[KeyDrawerGravity] = value }
    }

    val testField = getNullableFlow(KeyTestField)

    suspend fun setTestField(value: String?) {
        edit { if (value == null) it.remove(KeyTestField) else it[KeyTestField] = value }
    }

    val showSearchOptions = getFlow(KeyShowSearchOptions)

    suspend fun setShowSearchOptions(value: Boolean) {
        edit { it[KeyShowSearchOptions] = value }
    }

    val searchOptions = getFlow(KeySearchOptions, ::SearchOptions)

    suspend fun setSearchOptions(value: SearchOptions) {
        edit { it[KeySearchOptions] = value.toInt() }
    }

    val specialCharacters = getFlow(KeySpecialCharacters) {
        it.split(" ").toTypedArray()
    }

    suspend fun setSpecialCharacters(value: Array<String>) {
        edit { it[KeySpecialCharacters] = value.joinToString(separator = " ") }
    }

    val maxFileSizeForSearch = getFlow(KeyMaxSize)

    suspend fun setMaxFileSizeForSearch(value: Int) {
        edit { it[KeyMaxSize] = value }
    }

    val appUpdateCode = getFlow(KeyAppUpdateCode)

    suspend fun setAppUpdateCode(value: Int) {
        edit { it[KeyAppUpdateCode] = value }
    }

    val shownNotificationUpdateCode = getFlow(KeyShownNotificationUpdateCode)

    suspend fun setShownNotificationUpdateCode(value: Int) {
        edit { it[KeyShownNotificationUpdateCode] = value }
    }

    val maxDepthForSearch = getFlow(KeyMaxDepth)

    suspend fun setMaxDepthForSearch(value: Int) {
        edit { it[KeyMaxDepth] = value }
    }

    val deepBlack = getFlow(KeyDeepBlack)

    suspend fun setDeepBlack(value: Boolean) {
        edit { it[KeyDeepBlack] = value }
    }

    private val appThemeMode = getFlow(KeyAppTheme)

    val appTheme = data.map {
        val appThemeMode = it[KeyAppTheme] ?: AppTheme.defaultName()
        val deepBlack = it[KeyDeepBlack] ?: false
        AppTheme.fromString(appThemeMode, deepBlack)
    }.shareInOne(scope).asProperty()

    suspend fun setAppTheme(value: AppTheme) {
        edit { it[KeyAppTheme] = value.name }
    }

    val appOrientation = getFlow(KeyAppOrientation) {
        AppOrientation.entries[it.toInt()]
    }

    suspend fun setAppOrientation(value: AppOrientation) {
        edit { it[KeyAppOrientation] = value.ordinal.toString() }
    }

    val appLocale = getFlow(KeyLocale) {
        AppLocale.entries[it.toInt()]
    }

    suspend fun setAppLocale(value: AppLocale) {
        edit { it[KeyLocale] = value.ordinal.toString() }
    }

    val explorerItemComposition = getFlow(KeyExplorerItem) {
        ExplorerItemComposition(it)
    }

    suspend fun setExplorerItemComposition(value: ExplorerItemComposition) {
        edit { it[KeyExplorerItem] = value.flags }
    }

    val joystickComposition = getFlow(KeyJoystick) {
        JoystickComposition(it)
    }

    suspend fun setJoystickComposition(value: JoystickComposition) {
        edit { it[KeyJoystick] = value.data }
    }

    val hapticFeedback = getFlow(KeyHapticFeedback)

    suspend fun setHapticFeedback(value: Boolean) {
        edit { it[KeyHapticFeedback] = value }
    }

    val toyboxVariant = getFlow(KeyToybox, ToyboxVariant.Companion::invoke)

    suspend fun setEmbeddedToybox(value: ToyboxVariant) {
        edit { it[KeyToybox] = value.path }
    }

    private fun <V> getFlow(key: PreferenceKey<V>): StateFlowProperty<V> {
        return data.mapNotNull { it[key] ?: key.default }
            .shareInOne(scope)
            .asProperty()
    }

    private fun <V> getNullableFlow(key: PreferenceKey<V>): StateFlowProperty<V?> {
        return data.map { it[key] }
            .shareInOne(scope)
            .asProperty()
    }

    private fun <V,E> getFlow(key: PreferenceKey<V>, transformation: (V) -> E): StateFlowProperty<E> {
        return data.mapNotNull { (it[key] ?: key.default).let(transformation) }
            .shareInOne(scope)
            .asProperty()
    }

    private fun <T> Flow<T>.shareInOne(scope: CoroutineScope): SharedFlow<T> {
        return distinctUntilChanged().shareIn(scope, SharingStarted.Eagerly, replay = 1)
    }

    fun <T> getOrDefault(key: Preferences.Key<T>): T = preferences[key] ?: PreferenceKeys.default(key.name)
}
