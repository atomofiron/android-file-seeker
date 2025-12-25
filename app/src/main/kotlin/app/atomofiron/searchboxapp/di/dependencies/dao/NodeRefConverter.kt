package app.atomofiron.searchboxapp.di.dependencies.dao

import androidx.room.TypeConverter
import app.atomofiron.searchboxapp.model.explorer.NodeRef

object NodeRefConverter {

    @TypeConverter
    fun fromNodeRef(value: NodeRef): ByteArray = value.bytes

    @TypeConverter
    fun toNodeRef(value: ByteArray) = NodeRef(value)
}
