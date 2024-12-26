package app.atomofiron.searchboxapp.screens.finder.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.*
import app.atomofiron.searchboxapp.screens.finder.model.FinderItemType
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class FinderAdapter : ListAdapter<FinderStateItem, GeneralHolder<FinderStateItem>>(FinderDiffUtilCallback()) {

    lateinit var output: FinderAdapterOutput

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = currentList[position].stableId.toLong()

    override fun getItemViewType(position: Int): Int = currentList[position].layoutId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneralHolder<FinderStateItem> {
        return when (viewType) {
            FinderItemType.FIND.id -> FieldHolder(parent, viewType, output)
            FinderItemType.CHARACTERS.id -> CharactersHolder(parent, viewType, output)
            FinderItemType.CONFIGS.id -> ConfigHolder(parent, viewType, output)
            FinderItemType.TEST.id -> TestHolder(parent, viewType)
            FinderItemType.BUTTONS.id -> ButtonsHolder(parent, viewType, output)
            FinderItemType.PROGRESS.id -> ProgressHolder(parent, viewType, output)
            FinderItemType.TARGETS.id -> TargetsHolder(parent, viewType, output)
            FinderItemType.TIP.id -> TipHolder(parent, viewType)
            FinderItemType.DISCLAIMER.id -> DisclaimerHolder(parent, viewType)
            else -> throw IllegalArgumentException("viewType = $viewType")
        }
    }

    override fun onBindViewHolder(holder: GeneralHolder<FinderStateItem>, position: Int) {
        holder.bind(currentList[position], position)
    }
}