package app.atomofiron.searchboxapp.di.dependencies.interactor

import app.atomofiron.common.util.extension.debugRequireNotNull
import app.atomofiron.common.util.extension.set
import app.atomofiron.searchboxapp.di.dependencies.service.ExplorerService
import app.atomofiron.searchboxapp.di.dependencies.service.FinderService
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.ItemMatch
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.utils.ExplorerUtils.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class ResultInteractor(
    private val scope: CoroutineScope,
    private val utilService: UtilService,
    private val explorerService: ExplorerService,
    private val finderService: FinderService,
    private val finderStore: FinderStore,
) {
    private val dispatcher = Dispatchers.IO

    fun stop(uuid: UUID) = finderService.stop(uuid)

    fun copyToClipboard(item: Node) = utilService.copyToClipboard(item)

    fun deleteItems(items: List<Node>) {
        scope.launch(dispatcher) {
            explorerService.deleteEveryWhere(items)
        }
    }

    fun update(uuid: UUID, match: ItemMatch) {
        val updated = match.item.update(utilService.config())
        val match = match.update(updated)
        finderStore {
            update(uuid) {
                val result = result as? SearchResult.Files
                debugRequireNotNull(result) { "result is not Files" }
                result?.matches
                    ?.toMutableList()
                    ?.set(match) { it.item.ref == match.item.ref }
                    ?.let { copy(result = result.copy(matches = it, generation = result.generation.inc())) }
                    ?: this
            }
        }
    }
}