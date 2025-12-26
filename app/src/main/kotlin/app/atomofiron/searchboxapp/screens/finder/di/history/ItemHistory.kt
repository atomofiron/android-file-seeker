package app.atomofiron.searchboxapp.screens.finder.di.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

private const val LEGACY_QUERY_NAME = "title"

@Entity(tableName = HistoryDao.TABLE_NAME)
data class ItemHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = LEGACY_QUERY_NAME)
    val query: String = "",
    val pinned: Boolean = false,
)