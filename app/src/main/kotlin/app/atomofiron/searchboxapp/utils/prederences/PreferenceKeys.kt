package app.atomofiron.searchboxapp.utils.prederences

import android.view.Gravity
import app.atomofiron.searchboxapp.model.preference.AppOrientation
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.utils.Const

object PreferenceKeys {

    val KeyOpenedDirPath = PreferenceKey("pref_opened_dir_path", "")
    val KeyDockGravity = PreferenceKey("pref_drawer_gravity", Gravity.START)
    val KeyAppUpdateCode = PreferenceKey("pref_app_update_code", 0)
    val KeyLastUpdateNotificationCode = PreferenceKey("pref_last_upd_notification_code", 0)
    val KeySpecialCharacters = PreferenceKey("pref_special_characters", Const.DEFAULT_SPECIAL_CHARACTERS)
    val KeyAppOrientation = PreferenceKey("pref_app_orientation", AppOrientation.UNDEFINED.ordinal.toString())
    val KeyAppTheme = PreferenceKey("pref_app_theme", AppTheme.defaultName())
    val KeyDeepBlack = PreferenceKey("pref_deep_black", false)
    // it was long but DataStore made it int
    val KeyMaxSize = PreferenceKey("pref_max_size", Const.DEFAULT_MAX_SIZE)
    val KeyMaxDepth = PreferenceKey("pref_max_depth", Const.DEFAULT_MAX_DEPTH)
    val KeyExcludeDirs = PreferenceKey("pref_exclude_dirs", false)
    val KeyUseSu = PreferenceKey("pref_use_su", false)
    val KeyExplorerItem = PreferenceKey("pref_explorer_item", Const.DEFAULT_EXPLORER_ITEM)
    val KeyJoystick = PreferenceKey("pref_joystick", Const.DEFAULT_JOYSTICK)
    val KeyToybox = PreferenceKey("pref_toybox", setOf(Const.VALUE_TOYBOX_CUSTOM, Const.DEFAULT_TOYBOX_PATH))

    const val PREF_APP_UPDATE = "pref_app_update"
    const val PREF_EXPORT_IMPORT = "pref_export_import"
    const val PREF_LEAK_CANARY = "pref_leak_canary"
    const val PREF_CATEGORY_SYSTEM = "pref_category_system"
    const val PREF_CATEGORY_DEBUG = "pref_category_debug"

    private val keys by lazy(LazyThreadSafetyMode.NONE) {
        arrayOf(KeyOpenedDirPath, KeyDockGravity, KeyLastUpdateNotificationCode, KeySpecialCharacters, KeyAppOrientation, KeyAppTheme, KeyDeepBlack, KeyMaxSize, KeyMaxDepth, KeyExcludeDirs, KeyUseSu, KeyExplorerItem, KeyJoystick, KeyToybox)
    }

    fun <T> default(key: String): T = keys.find { it.key.name == key }?.default as T
}
