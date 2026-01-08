package app.atomofiron.searchboxapp.model.explorer.other

import androidx.room.Entity
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDao
import app.atomofiron.searchboxapp.model.explorer.NodeId
import app.atomofiron.searchboxapp.model.explorer.NodeSorting

@Entity(
    tableName = ExplorerDao.SORTING,
    primaryKeys = ["tabIndex", "rootId"],
)
data class TabRootSorting(
    val tabIndex: Int,
    val rootId: NodeId,
    val sorting: NodeSorting?,
)
