package app.atomofiron.searchboxapp.di.dependencies.db

import androidx.room.TypeConverter
import app.atomofiron.common.util.extension.decode
import app.atomofiron.common.util.extension.encode
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.finder.GlobalSearchResult
import app.atomofiron.searchboxapp.model.finder.QueryParams

object Converters {

    @TypeConverter
    fun fromNodeRef(value: NodeRef): ByteArray = value.bytes

    @TypeConverter
    fun toNodeRef(value: ByteArray) = NodeRef(value)

    @TypeConverter
    fun fromSearchResult(value: GlobalSearchResult): ByteArray = value.encode()

    @TypeConverter
    fun toSearchResult(value: ByteArray) = value.decode<GlobalSearchResult>()

    @TypeConverter
    fun fromQueryParams(value: QueryParams): ByteArray = value.encode()

    @TypeConverter
    fun toQueryParams(value: ByteArray) = value.decode<QueryParams>()

    @TypeConverter
    fun fromNodeSorting(value: NodeSorting?): String = when (value) {
        NodeSorting.Name.Reversed -> "-name"
        NodeSorting.Name -> "name"
        NodeSorting.Date.Reversed -> "-date"
        NodeSorting.Date -> "date"
        NodeSorting.Size.Reversed -> "-size"
        NodeSorting.Size -> "size"
        null -> ""
    }

    @TypeConverter
    fun toNodeSorting(value: String?): NodeSorting? = when (value) {
        "-name" -> NodeSorting.Name.Reversed
        "name" -> NodeSorting.Name
        "-date" -> NodeSorting.Date.Reversed
        "date" -> NodeSorting.Date
        "-size" -> NodeSorting.Size.Reversed
        "size" -> NodeSorting.Size
        else -> null
    }
}
