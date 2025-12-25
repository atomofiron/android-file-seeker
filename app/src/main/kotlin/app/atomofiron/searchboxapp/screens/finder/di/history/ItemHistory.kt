package app.atomofiron.searchboxapp.screens.finder.di.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = HistoryDao.TABLE_NAME)
data class ItemHistory(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "title")
    var query: String = "",
    var pinned: Boolean = false,
)