package app.atomofiron.searchboxapp.di.dependencies.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.atomofiron.searchboxapp.model.explorer.other.Deepest
import app.atomofiron.searchboxapp.model.explorer.other.TabRootSorting

@Dao
interface ExplorerDao {
    companion object {
        const val DEEPEST = "deepest"
        const val SORTING = "sorting"
    }

    @Query("SELECT * FROM $DEEPEST WHERE tabIndex = :tabIndex AND rootId = :rootId")
    fun getDeepest(tabIndex: Int, rootId: Int): Deepest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun put(item: Deepest): Long

    @Query("SELECT * FROM $SORTING WHERE tabIndex = :tabIndex AND rootId = :rootId")
    fun getSorting(tabIndex: Int, rootId: Int): TabRootSorting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun put(item: TabRootSorting): Long
}