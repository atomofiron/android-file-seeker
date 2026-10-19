package app.atomofiron.searchboxapp.di.dependencies.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.atomofiron.common.util.TaskId
import app.atomofiron.common.util.extension.decodeOrNull
import app.atomofiron.common.util.extension.encode
import app.atomofiron.searchboxapp.android.AbstractApp
import app.atomofiron.searchboxapp.di.dependencies.db.LegacyGlobalSearchResult2
import app.atomofiron.searchboxapp.di.dependencies.store.DefaultCharsetName
import app.atomofiron.searchboxapp.model.finder.GlobalSearchResult
import app.atomofiron.searchboxapp.model.finder.SearchResultCache
import app.atomofiron.searchboxapp.model.finder.SearchType
import java.io.File

@Dao
interface FinderDao {
    companion object {
        const val RESULT = "search"
        const val VERSION = 1
        const val DIR_NAME = "results"

        fun store(id: TaskId, result: GlobalSearchResult): Boolean = Store.store(id, result)
    }

    @Query("SELECT * FROM $RESULT WHERE version = $VERSION order by id")
    fun all(): List<SearchResultCache>

    @Query("SELECT * FROM $RESULT WHERE version = $VERSION AND id = :id")
    fun get(id: Int): SearchResultCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun put(item: SearchResultCache): Long

    @Query("DELETE FROM $RESULT WHERE id = :id")
    fun delete(id: TaskId) // don't use outside

    fun read(id: TaskId) = Store.read(id)

    fun drop(id: TaskId) {
        delete(id)
        Store.delete(id)
    }
}

private object Store {

    private const val MAX_CACHE_BYTES = 64L * 1024 * 1024

    private val dir = File(AbstractApp.appContext.cacheDir, FinderDao.DIR_NAME)

    private fun file(id: TaskId) = File(dir, "$id.bin")

    fun store(id: TaskId, result: GlobalSearchResult): Boolean {
        dir.mkdirs()
        val bytes = result.encode()
        val tmp = File(dir, "$id.bin.tmp")
        tmp.writeBytes(bytes)
        val dst = file(id)
        if (!tmp.renameTo(dst)) {
            tmp.delete()
            dst.writeBytes(bytes)
        }
        return dst.exists()
    }

    fun read(id: TaskId): GlobalSearchResult? {
        val file = file(id)
        if (!file.exists() || file.length() > MAX_CACHE_BYTES) {
            return null
        }
        val bytes = file.readBytes()
        return when {
            bytes.isLegacy() -> bytes.decodeOrNull<LegacyGlobalSearchResult2>()?.run {
                val type = if (forText) SearchType.Text(DefaultCharsetName) else SearchType.Names
                GlobalSearchResult(type, count, countTotal, matches, errors, generation)
            }
            else -> bytes.decodeOrNull<GlobalSearchResult>()
        }
    }

    fun delete(id: TaskId) {
        file(id).delete()
    }
}

private fun ByteArray.isLegacy(): Boolean {
    if (isEmpty()) return false
    // 0x08 = Boolean
    // 0x0A = Length-delimited = sealed class/object
    return first() == 0x08.toByte()
}
