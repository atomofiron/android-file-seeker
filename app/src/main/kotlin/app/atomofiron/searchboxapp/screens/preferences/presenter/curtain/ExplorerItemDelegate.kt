package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import app.atomofiron.fileseeker.BuildConfig.VERSION_NAME
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainPreferenceExplorerItemBinding
import app.atomofiron.searchboxapp.custom.drawable.setStrokedBackground
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.di.dependencies.store.Strings
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeMeta
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainId
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinder
import app.atomofiron.searchboxapp.screens.preferences.PreferenceScope
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.Const.DOT_APK
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding
import javax.inject.Inject

@PreferenceScope
class ExplorerItemDelegate @Inject constructor(
    private val preferenceStore: PreferenceStore,
    resources: AppResources,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>(), Strings by resources {

    private val dir = run {
        val meta = NodeMeta("drwxrwx---", "owner", "group", "4K", "2038-01-19", "03:14")
        val dirContent = NodeContent.Directory()
        val fileContent = NodeContent.Unknown
        val items = Array(17) { Node(NodeRef.Stub, NodeRef.Stub, content = if (it < 3) dirContent else fileContent) }.toList()
        val children = NodeChildren(items.toMutableList())
        Node(ref = NodeRef("Android"), meta = meta, content = dirContent, children = children)
    }
    private val file = run {
        val appName = get(R.string.app_name)
        val versionName = get(R.string.version_name)
            .split(' ').first()
        val name = "$appName $versionName$DOT_APK".replace(' ', '_')
        val meta = NodeMeta("drwxrwx---", "owner", "group", "47K", "2038-01-19", "03:14")
        val apkInfo = ApkInfo(Thumbnail(R.mipmap.ic_launcher), "", VERSION_NAME, 0, "", 0, 0, null, 0, null, withIcon = true, withSignature = false)
        val content = NodeContent.AndroidApp.apk(NodeRef(""), apkInfo)
        Node(ref = NodeRef(name), meta = meta, content = content)
    }

    private var composition = preferenceStore.explorerItemComposition.value

    override fun getHolder(inflater: LayoutInflater, id: CurtainId): CurtainApi.ViewHolder {
        val binding = CurtainPreferenceExplorerItemBinding.inflate(inflater, null, false)
        binding.init()
        binding.root.insetsPadding(ExtType.curtain, vertical = true)
        return CurtainApi.ViewHolder(binding.root)
    }

    private fun CurtainPreferenceExplorerItemBinding.init() {
        chipDetails.isChecked = composition.visibleDetails
        chipAccess.isChecked = composition.visibleAccess
        chipOwner.isChecked = composition.visibleOwner
        chipGroup.isChecked = composition.visibleGroup
        chipDate.isChecked = composition.visibleDate
        chipTime.isChecked = composition.visibleTime
        chipSize.isChecked = composition.visibleSize
        chipBox.isChecked = composition.visibleBox
        chipAlternating.isChecked = composition.visibleBg

        demoItems.setStrokedBackground(vertical = R.dimen.padding_half)
        val dirBinder = ExplorerItemBinder(explorerDir)
        val fileBinder = ExplorerItemBinder(explorerFile)
        val holders = arrayOf(dirBinder, fileBinder)
        val onClickListener = Listener(*holders)
        chipDetails.setOnClickListener(onClickListener)
        chipAccess.setOnClickListener(onClickListener)
        chipOwner.setOnClickListener(onClickListener)
        chipGroup.setOnClickListener(onClickListener)
        chipDate.setOnClickListener(onClickListener)
        chipTime.setOnClickListener(onClickListener)
        chipSize.setOnClickListener(onClickListener)
        chipBox.setOnClickListener(onClickListener)
        chipAlternating.setOnClickListener(onClickListener)

        dirBinder.bind(dir)
        fileBinder.bind(file)
        holders.bind()

        explorerDir.icon.alpha = Alpha.VISIBLE
        explorerDir.size.text = dir.size
    }

    private fun Array<out ExplorerItemBinder>.bind() {
        forEachIndexed { index, holder ->
            holder.disableClicks()
            holder.bindComposition(composition)
            holder.showAlternatingBackground(composition.visibleBg && (index % 2 != size % 2))
        }
    }

    private inner class Listener(
        private vararg val binders: ExplorerItemBinder,
    ) : View.OnClickListener {

        override fun onClick(view: View) {
            view as CompoundButton
            val isChecked = view.isChecked
            composition = when (view.id) {
                R.id.chip_details -> composition.copy(visibleDetails = isChecked)
                R.id.chip_access -> composition.copy(visibleAccess = isChecked)
                R.id.chip_owner -> composition.copy(visibleOwner = isChecked)
                R.id.chip_group -> composition.copy(visibleGroup = isChecked)
                R.id.chip_date -> composition.copy(visibleDate = isChecked)
                R.id.chip_time -> composition.copy(visibleTime = isChecked)
                R.id.chip_size -> composition.copy(visibleSize = isChecked)
                R.id.chip_box -> composition.copy(visibleBox = isChecked)
                R.id.chip_alternating -> composition.copy(visibleBg = isChecked)
                else -> throw Exception()
            }
            binders.bind()
            preferenceStore { setExplorerItemComposition(composition) }
        }
    }
}