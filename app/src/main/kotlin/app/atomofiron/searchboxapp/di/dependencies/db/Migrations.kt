package app.atomofiron.searchboxapp.di.dependencies.db

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.atomofiron.common.util.extension.decode
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDao
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.finder.GlobalSearchResult
import app.atomofiron.searchboxapp.model.finder.ItemMatch
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
                    result BLOB NOT NULL,
                    version INTEGER NOT NULL
                )
            """)
            db.execIndentless("""
                INSERT INTO $TMP (stopped, params, result, version)
                SELECT stopped, params, result, version FROM ${FinderDao.RESULT}
            """)
            db.execSQL("DROP TABLE ${FinderDao.RESULT}")
            db.execSQL("ALTER TABLE $TMP RENAME TO ${FinderDao.RESULT}")
        }
    }
    fun from4to5(context: Context) = object : Migration(4, 5) {
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
                INSERT INTO $TMP (stopped, params, sorting, version)
                SELECT stopped, params, '', version FROM ${FinderDao.RESULT}
            """)
            db.query("SELECT id, result FROM ${FinderDao.RESULT}").use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow("id")
                val resultIndex = cursor.getColumnIndexOrThrow("result")

                val provider = ResultProvider(context)
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(idIndex)
                    val bytes = cursor.getBlob(resultIndex)

                    val lr = bytes.decode<LegacyGlobalSearchResult>()
                    val result = GlobalSearchResult(lr.forText, lr.count, lr.countTotal, lr.matches, lr.errors, lr.generation)
                    provider.store(id, result)
                    db.compileStatement("UPDATE $TMP SET sorting = ? WHERE id = ?").run {
                        clearBindings()
                        bindString(1, Converters.fromNodeSorting(lr.sorting))
                        bindLong(2, id.toLong())
                        executeUpdateDelete()
                    }
                }
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
