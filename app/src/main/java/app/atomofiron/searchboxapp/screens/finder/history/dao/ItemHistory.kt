package app.atomofiron.searchboxapp.screens.finder.history.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = HistoryDao.TABLE_NAME)
data class ItemHistory(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var title: String = "",
    var pinned: Boolean = false,
)