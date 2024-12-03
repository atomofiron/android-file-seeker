package app.atomofiron.searchboxapp.injectable.interactor

import app.atomofiron.searchboxapp.injectable.service.ExplorerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import app.atomofiron.searchboxapp.injectable.service.ResultService
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItem
import java.util.*

class ResultInteractor(
    private val scope: CoroutineScope,
    private val resultService: ResultService,
    private val explorerService: ExplorerService,
) {
    private val dispatcher = Dispatchers.IO

    fun stop(uuid: UUID) = resultService.stop(uuid)

    fun copyToClipboard(item: Node) = resultService.copyToClipboard(item)

    fun deleteItems(items: List<Node>) {
        scope.launch(dispatcher) {
            explorerService.deleteEveryWhere(items)
        }
    }
}