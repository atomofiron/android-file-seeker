package app.atomofiron.searchboxapp.injectable.store

import android.content.Context
import android.view.Gravity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import app.atomofiron.common.util.flow.SharedFlowProperty
import app.atomofiron.common.util.flow.asProperty
import app.atomofiron.searchboxapp.model.preference.*
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyAppOrientation
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyAppTheme
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyDeepBlack
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyDockGravity
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyExcludeDirs
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyExplorerItem
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyHomeScreen
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyJoystick
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyMaxDepth
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyMaxSize
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyOpenedDirPath
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeySpecialCharacters
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyToybox
import app.atomofiron.searchboxapp.utils.PreferenceKeys.KeyUseSu
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

    lateinit var preferences: Preferences

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

    val excludeDirs = getFlow(KeyExcludeDirs)

    suspend fun setExcludeDirs(value: Boolean) {
        edit { it[KeyExcludeDirs] = value }
    }

    val maxFileSizeForSearch = getFlow(KeyMaxSize)

    suspend fun setMaxFileSizeForSearch(value: Int) {
        edit { it[KeyMaxSize] = value }
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
        AppOrientation.values()[it.toInt()]
    }

    suspend fun setAppOrientation(value: AppOrientation) {
        edit { it[KeyAppOrientation] = value.ordinal.toString() }
    }

    val homeScreen = getFlow(KeyHomeScreen) {
        HomeScreen.values()[it.toInt()]
    }

    suspend fun setHomeScreen(value: HomeScreen) {
        edit { it[KeyHomeScreen] = value.ordinal.toString() }
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

    val toyboxVariant = getFlow(KeyToybox) {
        ToyboxVariant.fromSet(context, it)
    }

    suspend fun setToyboxVariant(value: Set<String>) {
        edit { it[KeyToybox] = value }
    }

    private fun <V> getFlow(key: Preferences.Key<V>): SharedFlowProperty<V> {
        return data.map { it[key] ?: key.default() }.shareInOne(scope).asProperty()
    }

    private fun <V,E> getFlow(key: Preferences.Key<V>, transformation: (V) -> E): SharedFlowProperty<E> {
        return data.map { (it[key] ?: key.default()).let(transformation) }.shareInOne(scope).asProperty()
    }

    private fun <T> Flow<T>.shareInOne(scope: CoroutineScope): SharedFlow<T> {
        return distinctUntilChanged().shareIn(scope, SharingStarted.Eagerly, replay = 1)
    }

    fun <T> getOrDefault(key: Preferences.Key<T>): T = preferences[key] ?: key.default()

    @Suppress("UNCHECKED_CAST")
    fun <T> Preferences.Key<T>.default(): T {
        return when (this) {
            KeyOpenedDirPath -> "" as T
            KeyDockGravity -> Gravity.START as T
            KeySpecialCharacters -> Const.DEFAULT_SPECIAL_CHARACTERS as T
            KeyAppOrientation -> AppOrientation.UNDEFINED.ordinal.toString() as T
            KeyAppTheme -> AppTheme.defaultName() as T
            KeyDeepBlack -> false as T
            KeyMaxSize -> Const.DEFAULT_MAX_SIZE as T
            KeyMaxDepth -> Const.DEFAULT_MAX_DEPTH as T
            KeyExcludeDirs -> false as T
            KeyUseSu -> false as T
            KeyExplorerItem -> Const.DEFAULT_EXPLORER_ITEM as T
            KeyJoystick -> Const.DEFAULT_JOYSTICK as T
            KeyToybox -> setOf(Const.VALUE_TOYBOX_CUSTOM, Const.DEFAULT_TOYBOX_PATH) as T
            KeyHomeScreen -> HomeScreen.Search.ordinal.toString() as T
            else -> null as T
        }
    }
}
