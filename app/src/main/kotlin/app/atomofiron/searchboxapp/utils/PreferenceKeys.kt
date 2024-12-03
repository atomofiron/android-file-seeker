package app.atomofiron.searchboxapp.utils

import androidx.datastore.preferences.core.*

object PreferenceKeys {

    val KeyOpenedDirPath = stringPreferencesKey("pref_opened_dir_path")
    val KeyDockGravity = intPreferencesKey("pref_drawer_gravity")
    val KeySpecialCharacters = stringPreferencesKey("pref_special_characters")
    val KeyAppOrientation = stringPreferencesKey("pref_app_orientation")
    val KeyHomeScreen = stringPreferencesKey("pref_home_screen")
    val KeyAppTheme = stringPreferencesKey("pref_app_theme")
    val KeyDeepBlack = booleanPreferencesKey("pref_deep_black")
    // it was long but DataStore made it int
    val KeyMaxSize = intPreferencesKey("pref_max_size")
    val KeyMaxDepth = intPreferencesKey("pref_max_depth")
    val KeyExcludeDirs = booleanPreferencesKey("pref_exclude_dirs")
    val KeyUseSu = booleanPreferencesKey("pref_use_su")
    val KeyExplorerItem = intPreferencesKey("pref_explorer_item")
    val KeyJoystick = intPreferencesKey("pref_joystick")
    val KeyToybox = stringSetPreferencesKey("pref_toybox")

    const val PREF_EXPORT_IMPORT = "pref_export_import"
    const val PREF_LEAK_CANARY = "pref_leak_canary"
    const val PREF_CATEGORY_SYSTEM = "pref_category_system"
    const val PREF_CATEGORY_DEBUG = "pref_category_debug"
}
