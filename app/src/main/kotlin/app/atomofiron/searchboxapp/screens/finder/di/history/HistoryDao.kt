package app.atomofiron.searchboxapp.screens.finder.di.history

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    companion object {
        const val TABLE_NAME = "SearchHistory"
    }
    @get:Query("SELECT * FROM $TABLE_NAME order by pinned desc, id desc")
    val flow: Flow<List<ItemHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: ItemHistory): Long

    @Delete
    fun delete(item: ItemHistory)
}