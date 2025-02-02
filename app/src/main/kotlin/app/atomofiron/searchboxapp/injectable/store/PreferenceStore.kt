package app.atomofiron.searchboxapp.injectable.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import app.atomofiron.common.util.flow.StateFlowProperty
import app.atomofiron.common.util.flow.asProperty
import app.atomofiron.searchboxapp.model.preference.*
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKey
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyActionApk
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyAppOrientation
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyAppTheme
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyAppUpdateCode
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyDeepBlack
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyDockGravity
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyExplorerItem
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyJoystick
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyMaxDepth
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyMaxSize
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyOpenedDirPath
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeySpecialCharacters
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyToybox
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyUseSu
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys.KeyShownNotificationUpdateCode
import app.atomofiron.searchboxapp.utils.prederences.get
import app.atomofiron.searchboxapp.utils.prederences.remove
import app.atomofiron.searchboxapp.utils.prederences.set
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

    val actionApk = getFlow(KeyActionApk) {
        ActionApk.entries[it]
    }

    suspend fun setActionApk(value: ActionApk) {
        edit { it[KeyActionApk] = value.ordinal }
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

    val dockGravity = getFlow(KeyDockGravity)

    suspend fun setDockGravity(value: Int) {
        edit { it[KeyDockGravity] = value }
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

    val toyboxVariant = getFlow(KeyToybox, ToyboxVariant.Companion::invoke)

    suspend fun setEmbeddedToybox(value: ToyboxVariant) {
        edit { it[KeyToybox] = value.path }
    }

    private fun <V> getFlow(key: PreferenceKey<V>): StateFlowProperty<V> {
        return data.mapNotNull { it[key] ?: key.default }
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
