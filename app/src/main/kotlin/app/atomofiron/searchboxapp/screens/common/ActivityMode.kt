package app.atomofiron.searchboxapp.screens.common

import android.net.Uri
import app.atomofiron.searchboxapp.model.explorer.NodeContent

sealed class ActivityMode(
    val default: Boolean = false,
) {

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

    fun mimeFilters(): List<String>? = when (this) {
        is Default -> null
        is Share -> mimes
        is Receive -> listOf(NodeContent.Directory.MIME_TYPE)
    }
}
