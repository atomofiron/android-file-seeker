package app.atomofiron.searchboxapp.injectable.interactor

import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import app.atomofiron.searchboxapp.injectable.service.FinderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.service.UtilService
import app.atomofiron.searchboxapp.model.explorer.Node
import java.util.*

class ResultInteractor(
    private val scope: CoroutineScope,
    private val utilService: UtilService,
    private val explorerService: ExplorerService,
    private val finderService: FinderService,
) {
    private val dispatcher = Dispatchers.IO

    fun stop(uuid: UUID) = finderService.stop(uuid)

    fun copyToClipboard(item: Node) = utilService.copyToClipboard(item)

    fun deleteItems(items: List<Node>) {
        scope.launch(dispatcher) {
            explorerService.deleteEveryWhere(items)
        }
    }
}