package app.atomofiron.searchboxapp.screens.finder.di

import app.atomofiron.searchboxapp.di.dependencies.service.FinderService
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.model.finder.GenericSearchTask
import app.atomofiron.searchboxapp.screens.finder.FinderScope
import java.util.UUID
import javax.inject.Inject

@FinderScope
class FinderInteractor @Inject constructor(
    private val finderService: FinderService,
) {
    fun search(query: String, where: List<NodeRef>, config: SearchOptions) = finderService.search(query, where, config)

    fun stop(uuid: UUID) = finderService.stop(uuid)

    fun drop(task: GenericSearchTask) = finderService.drop(task)
}