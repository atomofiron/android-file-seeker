package app.atomofiron.searchboxapp.screens.licenses.state

sealed interface LicenseContent {
    @JvmInline
    value class Url(val value: String) : LicenseContent
    @JvmInline
    value class Text(val value: String) : LicenseContent
}
