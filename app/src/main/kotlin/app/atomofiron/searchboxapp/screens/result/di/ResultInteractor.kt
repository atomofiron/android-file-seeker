package app.atomofiron.searchboxapp.screens.result.di

import app.atomofiron.searchboxapp.di.dependencies.service.FinderService
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeMeta
import app.atomofiron.searchboxapp.screens.result.ResultScope
import app.atomofiron.searchboxapp.utils.ExplorerUtils.update
import app.atomofiron.searchboxapp.utils.ExplorerUtils.updateUsage
import java.util.UUID
import javax.inject.Inject

@ResultScope
class ResultInteractor @Inject constructor(
    private val finderService: FinderService,
    preferences: PreferenceStore,
) {
    private val asSu by preferences.asSu

    fun stop(uuid: UUID) = finderService.stop(uuid)

    fun usage(item: Node): NodeMeta = item.updateUsage(asSu)

    fun update(item: Node): Node = item.update(asSu)
}