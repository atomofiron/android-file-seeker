package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.view.LayoutInflater
import android.view.View
import android.widget.*
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainPreferenceExplorerItemBinding
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeProperties
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding

class ExplorerItemDelegate(
    private val preferenceStore: PreferenceStore
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {
    private val dir = run {
        val properties = NodeProperties("drwxrwx---", "owner", "group", "4K", "2038-01-19", "03:14", "Android")
        val dirContent = NodeContent.Directory()
        val fileContent = NodeContent.File.Unknown
        val items = Array(3) { Node("", "", content = dirContent) }.toList() +
                Array(14) { Node("", "", content = fileContent) }.toList()
        val children = NodeChildren(items.toMutableList(), isOpened = false)
        Node(path = "/sdcard/Android/", parentPath = "/sdcard/", properties = properties, content = dirContent, children = children)
    }

    private var composition = preferenceStore.explorerItemComposition.value

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainPreferenceExplorerItemBinding.inflate(inflater, null, false)
        binding.init()
        binding.root.insetsPadding(ExtType.curtain, vertical = true)
        return CurtainApi.ViewHolder(binding.root)
    }

    private fun CurtainPreferenceExplorerItemBinding.init() {
        preferenceDetails.isChecked = composition.visibleDetails
        preferenceAccess.isChecked = composition.visibleAccess
        preferenceOwner.isChecked = composition.visibleOwner
        preferenceGroup.isChecked = composition.visibleGroup
        preferenceDate.isChecked = composition.visibleDate
        preferenceTime.isChecked = composition.visibleTime
        preferenceSize.isChecked = composition.visibleSize
        preferenceBox.isChecked = composition.visibleBox
        preferenceBg.isChecked = composition.visibleBg

        val holder = ExplorerHolder(preferenceExplorerItem.root)
        val onClickListener = Listener(holder)
        preferenceDetails.setOnClickListener(onClickListener)
        preferenceAccess.setOnClickListener(onClickListener)
        preferenceOwner.setOnClickListener(onClickListener)
        preferenceGroup.setOnClickListener(onClickListener)
        preferenceDate.setOnClickListener(onClickListener)
        preferenceTime.setOnClickListener(onClickListener)
        preferenceSize.setOnClickListener(onClickListener)
        preferenceBox.setOnClickListener(onClickListener)
        preferenceBg.setOnClickListener(onClickListener)

        holder.bind(dir)
        holder.bindComposition(composition)
        holder.disableClicks()
        holder.setGreyBackgroundColor(composition.visibleBg)

        preferenceExplorerItem.itemExplorerIvIcon.alpha = Alpha.VISIBLE
        preferenceExplorerItem.itemExplorerTvSize.text = dir.size
    }

    private inner class Listener(
        private val holder: ExplorerHolder,
    ) : View.OnClickListener {

        override fun onClick(view: View) {
            view as CompoundButton
            val isChecked = view.isChecked
            composition = when (view.id) {
                R.id.preference_details -> composition.copy(visibleDetails = isChecked)
                R.id.preference_access -> composition.copy(visibleAccess = isChecked)
                R.id.preference_owner -> composition.copy(visibleOwner = isChecked)
                R.id.preference_group -> composition.copy(visibleGroup = isChecked)
                R.id.preference_date -> composition.copy(visibleDate = isChecked)
                R.id.preference_time -> composition.copy(visibleTime = isChecked)
                R.id.preference_size -> composition.copy(visibleSize = isChecked)
                R.id.preference_box -> composition.copy(visibleBox = isChecked)
                R.id.preference_bg -> composition.copy(visibleBg = isChecked)
                else -> throw Exception()
            }
            holder.bindComposition(composition, preview = true)
            holder.setGreyBackgroundColor(composition.visibleBg)
            preferenceStore { setExplorerItemComposition(composition) }
        }
    }
}