package app.atomofiron.searchboxapp.di.dependencies.interactor

import app.atomofiron.searchboxapp.di.dependencies.service.FinderService
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.model.finder.SearchTask
import java.util.*

class FinderInteractor(private val finderService: FinderService) {
    fun search(query: String, where: List<Node>, config: SearchOptions) {
        finderService.search(query, where, config)
    }

    fun stop(uuid: UUID) = finderService.stop(uuid)

    fun drop(task: SearchTask) = finderService.drop(task)
}