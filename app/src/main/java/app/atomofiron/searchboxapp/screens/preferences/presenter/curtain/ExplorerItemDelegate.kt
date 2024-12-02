package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.view.LayoutInflater
import android.view.View
import android.widget.*
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.CurtainPreferenceExplorerItemBinding
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeProperties
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.utils.Const
import lib.atomofiron.insets.insetsPadding

class ExplorerItemDelegate(
    private val preferenceStore: PreferenceStore
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {
    private val dir = Node(
        path = "/sdcard/Android/",
        parentPath = "/sdcard/",
        properties = NodeProperties("drwxrwx---", "atomofiron", "everybody", "4K", "19-01-2038", "03:14", "Android"),
        content = NodeContent.Directory(),
    )

    private var composition = preferenceStore.explorerItemComposition.value

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainPreferenceExplorerItemBinding.inflate(inflater, null, false)
        binding.init()
        binding.root.insetsPadding(vertical = true)
        return CurtainApi.ViewHolder(binding.root)
    }

    private fun CurtainPreferenceExplorerItemBinding.init() {
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

        preferenceExplorerItem.itemExplorerIvIcon.alpha = Const.ALPHA_VISIBLE
        preferenceExplorerItem.itemExplorerTvSize.text = dir.size
    }

    private inner class Listener(
        private val holder: ExplorerHolder,
    ) : View.OnClickListener {

        override fun onClick(view: View) {
            view as CompoundButton
            val isChecked = view.isChecked
            composition = when (view.id) {
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