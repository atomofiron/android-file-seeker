package app.atomofiron.searchboxapp.model.finder

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchParams(
    val query: String,
    val useRegex: Boolean,
    val ignoreCase: Boolean,
) : Parcelable