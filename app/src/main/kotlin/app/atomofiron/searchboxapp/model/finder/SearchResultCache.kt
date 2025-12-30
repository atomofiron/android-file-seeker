package app.atomofiron.searchboxapp.model.finder

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao

@Entity(tableName = FinderDao.RESULT)
data class SearchResultCache(
    @PrimaryKey
    val id: Int,
    val stopped: Boolean,
    val params: QueryParams,
    val result: GlobalSearchResult,
    val version: Int = FinderDao.VERSION,
)