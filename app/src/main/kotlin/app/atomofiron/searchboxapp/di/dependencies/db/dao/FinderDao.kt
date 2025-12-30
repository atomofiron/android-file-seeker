package app.atomofiron.searchboxapp.di.dependencies.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import app.atomofiron.searchboxapp.model.finder.SearchResultCache

@Dao
interface FinderDao {
    companion object {
        const val RESULT = "search"
        const val VERSION = 0
    }

    @Query("SELECT * FROM $RESULT WHERE version = $VERSION order by id")
    fun all(): List<SearchResultCache>

    @Query("SELECT * FROM $RESULT WHERE version = $VERSION AND id = :id")
    fun get(id: Int): SearchResultCache?

    @Insert
    fun put(item: SearchResultCache): Long

    @Delete
    fun delete(item: SearchResultCache)

    @Query("DELETE FROM $RESULT WHERE :id = id")
    fun delete(id: Int)
}