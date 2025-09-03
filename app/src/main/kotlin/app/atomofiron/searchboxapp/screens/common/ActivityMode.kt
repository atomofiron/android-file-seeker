package app.atomofiron.searchboxapp.screens.common

import android.net.Uri

sealed class ActivityMode(
    val default: Boolean,
) {
    constructor() : this(default = false)

    data object Default : ActivityMode(default = true)

    data class Receive(
        val subject: String,
        val uris: List<Uri>,
        val texts: List<CharSequence>,
    ) : ActivityMode()

    data class Share(
        val initialUri: Uri?,
        val mimes: List<String>,
        val multiple: Boolean,
    ) : ActivityMode()
}
