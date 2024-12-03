package app.atomofiron.searchboxapp.screens.finder.history.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {
    companion object {
        const val TABLE_NAME = "SearchHistory"
    }
    @get:Query("SELECT * FROM $TABLE_NAME order by pinned desc, id desc")
    val all: List<ItemHistory>

    @Query("SELECT * FROM $TABLE_NAME WHERE id = :id")
    fun getById(id: Long): ItemHistory

    @Query("SELECT COUNT(*) FROM $TABLE_NAME")
    fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: ItemHistory): Long

    @Delete
    fun delete(item: ItemHistory)
}