package app.atomofiron.searchboxapp.di.dependencies.interactor

import app.atomofiron.searchboxapp.di.dependencies.service.ExplorerService
import app.atomofiron.searchboxapp.di.dependencies.service.FinderService
import app.atomofiron.searchboxapp.di.dependencies.service.UtilService
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeMeta
import app.atomofiron.searchboxapp.utils.ExplorerUtils.update
import app.atomofiron.searchboxapp.utils.ExplorerUtils.updateUsage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class ResultInteractor(
    private val scope: CoroutineScope,
    private val utilService: UtilService,
    private val explorerService: ExplorerService,
    private val finderService: FinderService,
    preferences: PreferenceStore,
) {
    private val dispatcher = Dispatchers.IO
    private val asSu by preferences.asSu

    fun stop(uuid: UUID) = finderService.stop(uuid)

    fun copyToClipboard(item: Node) = utilService.copyToClipboard(item, withAlert = false)

    fun deleteItems(items: List<Node>) {
        scope.launch(dispatcher) {
            explorerService.deleteEveryWhere(items)
        }
    }

    fun usage(item: Node): NodeMeta = item.updateUsage(asSu)

    fun update(item: Node): Node = item.update(asSu)
}