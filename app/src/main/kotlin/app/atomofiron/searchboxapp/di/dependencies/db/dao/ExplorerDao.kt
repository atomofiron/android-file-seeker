package app.atomofiron.searchboxapp.di.dependencies.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExplorerDao {
    companion object {
        const val DEEPEST = "deepest"
    }

    @Query("SELECT * FROM $DEEPEST WHERE tabIndex = :tabIndex AND rootId = :rootId")
    fun get(tabIndex: Int, rootId: Int): Deepest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun put(item: Deepest): Long
}