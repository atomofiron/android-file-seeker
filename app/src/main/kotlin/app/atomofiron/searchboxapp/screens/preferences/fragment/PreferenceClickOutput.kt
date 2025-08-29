package app.atomofiron.searchboxapp.screens.preferences.fragment

interface PreferenceClickOutput {
    fun onAboutClick()
    fun onColorSchemeClick()
    fun onExportImportClick()
    fun onExplorerItemClick()
    fun onJoystickClick()
    /** @return {@code true} to update the state of the preference with the new value */
    fun onUseSuChanged(value: Boolean): Boolean
}