package app.atomofiron.searchboxapp.screens.finder.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.ButtonsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.CharactersHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.DisclaimerHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditCharactersHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditMaxDepthHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditMaxSizeHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.OptionsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.QueryFieldHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TargetsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TaskHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TestHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TitleHolder
import app.atomofiron.searchboxapp.screens.finder.state.FinderItemType
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem

class FinderAdapter(
    private val output: FinderAdapterOutput,
) : ListAdapter<FinderStateItem, GeneralHolder<FinderStateItem>>(FinderDiffUtilCallback()) {

    var holderListener: AdapterHolderListener? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).stableId

    override fun getItemViewType(position: Int): Int = getItem(position).viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneralHolder<FinderStateItem> {
        return when (FinderItemType[viewType]) {
            FinderItemType.FIND -> QueryFieldHolder(parent, output)
            FinderItemType.CHARACTERS -> CharactersHolder(parent, output)
            FinderItemType.OPTIONS -> OptionsHolder(parent, output)
            FinderItemType.TITLE -> TitleHolder(parent)
            FinderItemType.TEST -> TestHolder(parent, output)
            FinderItemType.BUTTONS -> ButtonsHolder(parent, output)
            FinderItemType.PROGRESS -> TaskHolder(parent, output)
            FinderItemType.MAX_DEPTH -> EditMaxDepthHolder(parent, output)
            FinderItemType.MAX_SIZE -> EditMaxSizeHolder(parent, output)
            FinderItemType.EDIT_CHARS -> EditCharactersHolder(parent, output)
            FinderItemType.TARGETS -> TargetsHolder(parent, output)
            FinderItemType.DISCLAIMER -> DisclaimerHolder(parent)
            null -> throw IllegalArgumentException("viewType = $viewType")
        }.also { holderListener?.onCreate(it, viewType) }
    }

    override fun onBindViewHolder(holder: GeneralHolder<FinderStateItem>, position: Int) {
        holder.bind(getItem(position), position)
        holderListener?.onBind(holder, position)
    }
}