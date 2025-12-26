package app.atomofiron.searchboxapp.di.dependencies.db

import androidx.room.TypeConverter
import app.atomofiron.common.util.extension.decode
import app.atomofiron.common.util.extension.encode
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.GlobalSearchResult
import app.atomofiron.searchboxapp.model.finder.QueryParams

object NodeRefConverter {

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
}
