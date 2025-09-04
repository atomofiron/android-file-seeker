package app.atomofiron.searchboxapp.utils.preferences

import android.view.Gravity
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.model.preference.AppOrientation
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import app.atomofiron.searchboxapp.utils.Const

object PreferenceKeys {
    const val PREF_APP_UPDATE = "pref_app_update"
    const val PREF_EXPORT_IMPORT = "pref_export_import"
    const val PREF_LEAK_CANARY = "pref_leak_canary"
    const val PREF_CATEGORY_SYSTEM = "pref_category_system"
    const val PREF_CATEGORY_DEBUG = "pref_category_debug"
    const val PREF_COLOR_SCHEME = "pref_color_scheme"

    val KeyOpenedDirPath = PreferenceKey("pref_opened_dir_path", "")
    val KeyDrawerGravity = PreferenceKey("pref_drawer_gravity", Gravity.START)
    val KeyAppUpdateCode = PreferenceKey("pref_app_update_code", 0)
    val KeyShownNotificationUpdateCode = PreferenceKey("pref_shown_notification_update_code", 0)
    val KeyTestField = PreferenceKey("pref_test_field", "")
    val KeyShowSearchOptions = PreferenceKey("pref_show_search_options", true)
    val KeySearchOptions = PreferenceKey("pref_search_options", SearchOptions.DEFAULT)
    val KeySpecialCharacters = PreferenceKey("pref_special_characters", Const.DEFAULT_SPECIAL_CHARACTERS)
    val KeyAppOrientation = PreferenceKey("pref_app_orientation", AppOrientation.UNDEFINED.ordinal.toString())
    val KeyAppTheme = PreferenceKey("pref_app_theme", AppTheme.defaultName())
    val KeyDeepBlack = PreferenceKey("pref_deep_black", false)
    // todo pack KeyMaxSize and KeyMaxDepth with other in one
    // it was long but DataStore made it int
    val KeyMaxSize = PreferenceKey("pref_max_size", Const.DEFAULT_MAX_SIZE)
    val KeyMaxDepth = PreferenceKey("pref_max_depth", Const.DEFAULT_MAX_DEPTH)
    val KeyUseSu = PreferenceKey("pref_use_su", false)
    val KeyExplorerItem = PreferenceKey("pref_explorer_item", ExplorerItemComposition.DEFAULT)
    val KeyJoystick = PreferenceKey("pref_joystick", JoystickComposition.DEFAULT)
    val KeyHapticFeedback = PreferenceKey("pref_haptic_feedback", true)
    val KeyToybox = PreferenceKey("pref_toybox_path", ToyboxVariant.Undefined.EMPTY)

    private val keys by lazy(LazyThreadSafetyMode.NONE) {
        arrayOf(KeyOpenedDirPath, KeyDrawerGravity, KeyShownNotificationUpdateCode, KeySpecialCharacters, KeyAppOrientation, KeyAppTheme, KeyDeepBlack, KeyMaxSize, KeyMaxDepth, KeyUseSu, KeyExplorerItem, KeyJoystick, KeyHapticFeedback, KeyToybox)
    }

    @Suppress("UNCHECKED_CAST") // omg it's an error in Kotlin 2.2
    fun <T> default(key: String): T = keys.find { it.key.name == key }?.default as T
}
