package app.atomofiron.searchboxapp.model.finder

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class QueryParams(
    val query: String,
    val regex: Boolean,
    val ignoreCase: Boolean,
) : Parcelable