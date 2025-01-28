package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.core.view.isGone
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class ConfigHolder(
    parent: ViewGroup,
    layoutId: Int,
    private val listener: FinderConfigListener
) : GeneralHolder<FinderStateItem>(parent, layoutId) {

    private val cbCaseSense = itemView.findViewById<CheckBox>(R.id.config_cb_case_sense)
    private val cbUseRegexp = itemView.findViewById<CheckBox>(R.id.config_cb_use_regexp)
    private val cpSearchInContent = itemView.findViewById<CheckBox>(R.id.config_cb_in_content)
    private val cbExcludeDirs = itemView.findViewById<CheckBox>(R.id.config_cb_exclude_dirs)
    private val cbReplace = itemView.findViewById<CheckBox>(R.id.config_cb_replace)

    private var checkExcludeDirsWhenEnabled = false

    init {
        itemView.isFocusable = false
        itemView.isClickable = false

        cbCaseSense.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(config = it.config.copy(ignoreCase = !view.isChecked)) }
        }
        cbUseRegexp.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(config = it.config.copy(useRegex = view.isChecked)) }
        }
        cpSearchInContent.setOnClickListener { view ->
            view as CompoundButton
            val excludeDirs = when {
                !view.isChecked && checkExcludeDirsWhenEnabled -> true
                view.isChecked -> false
                else -> cbExcludeDirs.isChecked
            }
            checkExcludeDirsWhenEnabled = when {
                !view.isChecked && checkExcludeDirsWhenEnabled -> false
                view.isChecked -> cbExcludeDirs.isChecked
                else -> checkExcludeDirsWhenEnabled
            }
            update { it.copy(config = it.config.copy(searchInContent = view.isChecked, excludeDirs = excludeDirs)) }
            cbExcludeDirs.isEnabled = !view.isChecked
        }
        cbExcludeDirs.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(config = it.config.copy(excludeDirs = view.isChecked)) }
        }
        cbReplace.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(config = it.config.copy(replaceEnabled = view.isChecked)) }
        }
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.ConfigItem
        cbCaseSense.isChecked = !item.ignoreCase
        cbUseRegexp.isChecked = item.useRegex
        cpSearchInContent.isChecked = item.searchInContent
        cbExcludeDirs.isChecked = item.excludeDirs
        cbReplace.isChecked = item.replaceEnabled

        if (item.searchInContent && item.excludeDirs) {
            checkExcludeDirsWhenEnabled = true
            cbExcludeDirs.isChecked = false
        }

        if (item.isLocal) {
            cpSearchInContent.isGone = true
            cbExcludeDirs.isGone = true
        }
    }

    private fun update(block: (FinderStateItem.ConfigItem) -> FinderStateItem.ConfigItem) {
        var item = item as FinderStateItem.ConfigItem
        item = block(item)
        listener.onConfigChange(item)
    }

    interface FinderConfigListener {
        fun onConfigChange(item: FinderStateItem.ConfigItem)
    }
}