package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class ConfigHolder(
    parent: ViewGroup,
    id: Int,
    private val listener: OnActionListener
) : CardViewHolder(parent, id) {
    private val llConfig = itemView.findViewById<LinearLayoutCompat>(R.id.item_ll_config)
    private val cbCaseSense = itemView.findViewById<CheckBox>(R.id.config_cb_case_sense)
    private val cbUseRegexp = itemView.findViewById<CheckBox>(R.id.config_cb_use_regexp)
    private val cpSearchInContent = itemView.findViewById<CheckBox>(R.id.config_cb_in_content)
    private val cbExcludeDirs = itemView.findViewById<CheckBox>(R.id.config_cb_exclude_dirs)
    private val cbReplace = itemView.findViewById<CheckBox>(R.id.config_cb_replace)
    private val btnHistory = itemView.findViewById<Button>(R.id.item_config_history)
    private val btnVisibility = itemView.findViewById<Button>(R.id.item_config_visibility)

    private var checkExcludeDirsWhenEnabled = false

    init {
        itemView.isFocusable = false
        itemView.isClickable = false

        cbCaseSense.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(ignoreCase = !view.isChecked) }
        }
        cbUseRegexp.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(useRegex = view.isChecked) }
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
            update { it.copy(searchInContent = view.isChecked, excludeDirs = excludeDirs) }
            cbExcludeDirs.isEnabled = !view.isChecked
        }
        cbExcludeDirs.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(excludeDirs = view.isChecked) }
        }
        cbReplace.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(replaceEnabled = view.isChecked) }
        }
        btnHistory.setOnClickListener {
            listener.onHistoryClick()
        }
        btnVisibility.setOnClickListener {
            listener.onConfigVisibilityClick()
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
        llConfig.isVisible = !item.isCollapsed
    }

    private fun update(block: (FinderStateItem.ConfigItem) -> FinderStateItem.ConfigItem) {
        var item = item as FinderStateItem.ConfigItem
        item = block(item)
        listener.onConfigChange(item)
    }

    interface OnActionListener {
        fun onConfigChange(item: FinderStateItem.ConfigItem)
        fun onHistoryClick()
        fun onConfigVisibilityClick()
    }
}