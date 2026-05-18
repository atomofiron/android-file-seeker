package app.atomofiron.searchboxapp.di.dependencies.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.atomofiron.common.util.TaskId
import app.atomofiron.common.util.extension.decodeOrNull
import app.atomofiron.common.util.extension.encode
import app.atomofiron.searchboxapp.android.AbstractApp
import app.atomofiron.searchboxapp.model.finder.GlobalSearchResult
import app.atomofiron.searchboxapp.model.finder.SearchResultCache
import java.io.File

@Dao
interface FinderDao {
    companion object {
        const val RESULT = "search"
        const val VERSION = 1
        const val DIR_NAME = "results"

        fun store(id: TaskId, result: GlobalSearchResult) = Store.store(id, result)
    }

    @Query("SELECT * FROM $RESULT WHERE version = $VERSION order by id")
    fun all(): List<SearchResultCache>

    @Query("SELECT * FROM $RESULT WHERE version = $VERSION AND id = :id")
    fun get(id: Int): SearchResultCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun put(item: SearchResultCache): Long

    @Query("DELETE FROM $RESULT WHERE id = :id")
    fun delete(id: TaskId) // don't use outside

    fun store(item: SearchResultCache, result: GlobalSearchResult) {
        val id = put(item).toInt()
        Store.store(id, result)
    }

    fun read(id: TaskId) = Store.read(id)

    fun drop(id: TaskId) {
        delete(id)
        Store.delete(id)
    }
}

private object Store {

    private val dir = File(AbstractApp.appContext.cacheDir, FinderDao.DIR_NAME)

    private fun file(id: TaskId) = File(dir, "$id.bin")

    fun store(id: TaskId, result: GlobalSearchResult) {
        dir.mkdirs()
        val file = file(id)
        file.createNewFile()
        val bytes = result.encode()
        file.writeBytes(bytes)
    }

    fun read(id: TaskId): GlobalSearchResult? {
        return file(id)
            .takeIf { it.exists() }
            ?.readBytes()
            ?.decodeOrNull<GlobalSearchResult>()
    }

    fun delete(id: TaskId) {
        file(id).delete()
    }
}
