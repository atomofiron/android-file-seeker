package app.atomofiron.searchboxapp.work

import android.net.Uri
import app.atomofiron.searchboxapp.utils.UriListSerializer
import kotlinx.serialization.Serializable

@Serializable
data class ReceiveData(
    val subject: String,
    @Serializable(UriListSerializer::class)
    val uris: List<Uri>,
    val texts: List<String>,
    val destination: String,
)
