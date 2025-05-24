package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import app.atomofiron.common.util.property.StrongProperty
import app.atomofiron.fileseeker.BuildConfig.VERSION_NAME
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainPreferenceExplorerItemBinding
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeProperties
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding

class ExplorerItemDelegate(
    private val preferenceStore: PreferenceStore,
    private val resources: StrongProperty<Resources>,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {
    private val dir = run {
        val properties = NodeProperties("drwxrwx---", "owner", "group", "4K", "2038-01-19", "03:14", "Android")
        val dirContent = NodeContent.Directory()
        val fileContent = NodeContent.File.Unknown
        val items = Array(17) { Node("", "", content = if (it < 3) dirContent else fileContent) }.toList()
        val children = NodeChildren(items.toMutableList(), isOpened = false)
        Node(path = "", properties = properties, content = dirContent, children = children)
    }
    private val file = run {
        val name = "File Seeker ${resources.value.getString(R.string.version_name)}.apk"
        val properties = NodeProperties("drwxrwx---", "owner", "group", "47K", "2038-01-19", "03:14", name)
        val apkInfo = ApkInfo(Thumbnail(R.mipmap.ic_launcher), "", VERSION_NAME, 0, "", 0, 0, null, null)
        val content = NodeContent.File.AndroidApp.Apk(NodeRef(""), apkInfo)
        Node(path = "", properties = properties, content = content)
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

        val dirHolder = ExplorerHolder(preferenceExplorerDir.root)
        val fileHolder = ExplorerHolder(preferenceExplorerFile.root)
        val holders = arrayOf(dirHolder, fileHolder)
        val onClickListener = Listener(*holders)
        preferenceDetails.setOnClickListener(onClickListener)
        preferenceAccess.setOnClickListener(onClickListener)
        preferenceOwner.setOnClickListener(onClickListener)
        preferenceGroup.setOnClickListener(onClickListener)
        preferenceDate.setOnClickListener(onClickListener)
        preferenceTime.setOnClickListener(onClickListener)
        preferenceSize.setOnClickListener(onClickListener)
        preferenceBox.setOnClickListener(onClickListener)
        preferenceBg.setOnClickListener(onClickListener)

        dirHolder.bind(dir)
        fileHolder.bind(file)
        holders.bind()

        preferenceExplorerDir.itemExplorerIvIcon.alpha = Alpha.VISIBLE
        preferenceExplorerDir.itemExplorerTvSize.text = dir.size
    }

    private fun Array<out ExplorerHolder>.bind() {
        forEachIndexed { index, holder ->
            holder.disableClicks()
            holder.bindComposition(composition)
            holder.setGreyBackgroundColor(composition.visibleBg && (index % 2 != size % 2))
        }
    }

    private inner class Listener(
        private vararg val holders: ExplorerHolder,
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
            holders.bind()
            preferenceStore { setExplorerItemComposition(composition) }
        }
    }
}