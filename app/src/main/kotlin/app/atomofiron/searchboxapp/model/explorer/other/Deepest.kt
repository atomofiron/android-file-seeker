package app.atomofiron.searchboxapp.model.explorer.other

import androidx.room.Entity
import app.atomofiron.searchboxapp.di.dependencies.db.dao.ExplorerDao
import app.atomofiron.searchboxapp.model.explorer.NodeId
import app.atomofiron.searchboxapp.model.explorer.NodeRef

@Entity(
    tableName = ExplorerDao.DEEPEST,
    primaryKeys = ["tabIndex", "rootId"],
)
data class Deepest(
    val tabIndex: Int,
    val rootId: NodeId,
    val ref: NodeRef,
)
