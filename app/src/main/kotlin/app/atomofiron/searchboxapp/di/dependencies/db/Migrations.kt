package app.atomofiron.searchboxapp.di.dependencies.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.atomofiron.common.util.extension.decodeOrNull
import app.atomofiron.common.util.extension.logE
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDao
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao
import app.atomofiron.searchboxapp.di.dependencies.store.DefaultCharsetName
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.finder.GlobalSearchResult
import app.atomofiron.searchboxapp.model.finder.ItemMatch
import app.atomofiron.searchboxapp.model.finder.SearchType
import kotlinx.serialization.Serializable

private const val TMP = "tmp"

object Migrations {
    fun from2to3() = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execIndentless("""
                CREATE TABLE ${ExplorerDao.SORTING} (
                    tabIndex INTEGER NOT NULL,
                    rootId INTEGER NOT NULL,
                    sorting TEXT,
                    PRIMARY KEY(tabIndex, rootId)
                )
            """)
        }
    }
    fun from3to4() = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execIndentless("""
                CREATE TABLE $TMP (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    stopped INTEGER NOT NULL,
                    params BLOB NOT NULL,
                    sorting TEXT NOT NULL,
                    version INTEGER NOT NULL
                )
            """)
            db.execIndentless("""
                INSERT INTO $TMP (id, stopped, params, sorting, version)
                SELECT id, stopped, params, '', version FROM ${FinderDao.RESULT}
            """)
            val updateSorting = db.compileStatement("UPDATE $TMP SET sorting = ? WHERE id = ?")
            val deleteRow = db.compileStatement("DELETE FROM $TMP WHERE id = ?")
            try {
                db.query("SELECT id, result FROM ${FinderDao.RESULT}").use { cursor ->
                    val idIndex = cursor.getColumnIndexOrThrow("id")
                    val resultIndex = cursor.getColumnIndexOrThrow("result")

                    while (cursor.moveToNext()) {
                        val id = cursor.getInt(idIndex)
                        val bytes = cursor.getBlob(resultIndex)
                        val lr = bytes?.decodeOrNull<LegacyGlobalSearchResult>()
                        if (lr == null) {
                            logE("Migration 3->4: cached result $id is not readable")
                            deleteRow.clearBindings()
                            deleteRow.bindLong(1, id.toLong())
                            deleteRow.executeUpdateDelete()
                            continue
                        }
                        val type = if (lr.forText) SearchType.Text(DefaultCharsetName) else SearchType.Names
                        val result = GlobalSearchResult(type, lr.count, lr.countTotal, lr.matches, lr.errors, lr.generation)
                        FinderDao.store(id, result)
                        updateSorting.clearBindings()
                        updateSorting.bindString(1, Converters.fromNodeSorting(lr.sorting))
                        updateSorting.bindLong(2, id.toLong())
                        updateSorting.executeUpdateDelete()
                    }
                }
            } finally {
                updateSorting.close()
                deleteRow.close()
            }
            db.execSQL("DROP TABLE ${FinderDao.RESULT}")
            db.execSQL("ALTER TABLE $TMP RENAME TO ${FinderDao.RESULT}")
        }
    }
}

private fun SupportSQLiteDatabase.execIndentless(string: String) = execSQL(string.trimIndent())

@Serializable
data class LegacyGlobalSearchResult(
    val forText: Boolean,
    val count: Int = 0,
    val countTotal: Int = 0,
    val matches: List<ItemMatch>,
    val errors: List<String>,
    val sorting: NodeSorting = NodeSorting.Date,
    val generation: Int = 0,
)

@Serializable
data class LegacyGlobalSearchResult2(
    val forText: Boolean,
    val count: Int = 0,
    val countTotal: Int = 0,
    val matches: List<ItemMatch> = listOf(),
    val errors: List<String> = listOf(),
    val generation: Int = 0,
)
