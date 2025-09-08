package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import app.atomofiron.common.util.property.StrongProperty
import app.atomofiron.fileseeker.BuildConfig.VERSION_NAME
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainPreferenceExplorerItemBinding
import app.atomofiron.searchboxapp.custom.drawable.setStrokedBackground
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeProperties
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinder
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl
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
        val fileContent = NodeContent.Unknown
        val items = Array(17) { Node("", "", content = if (it < 3) dirContent else fileContent) }.toList()
        val children = NodeChildren(items.toMutableList())
        Node(path = "", properties = properties, content = dirContent, children = children)
    }
    private val file = run {
        val appName = resources.value.getString(R.string.app_name)
        val versionName = resources.value.getString(R.string.version_name)
            .split(' ').first()
        val name = "$appName $versionName.apk".replace(' ', '_')
        val properties = NodeProperties("drwxrwx---", "owner", "group", "47K", "2038-01-19", "03:14", name)
        val apkInfo = ApkInfo(Thumbnail(R.mipmap.ic_launcher), "", VERSION_NAME, 0, "", 0, 0, null, 0, null)
        val content = NodeContent.AndroidApp.apk(NodeRef(""), apkInfo)
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
        details.isChecked = composition.visibleDetails
        access.isChecked = composition.visibleAccess
        owner.isChecked = composition.visibleOwner
        group.isChecked = composition.visibleGroup
        date.isChecked = composition.visibleDate
        time.isChecked = composition.visibleTime
        size.isChecked = composition.visibleSize
        box.isChecked = composition.visibleBox
        alternating.isChecked = composition.visibleBg

        demoItems.setStrokedBackground(vertical = R.dimen.padding_half)
        val dirBinder = ExplorerItemBinderImpl(explorerDir.root)
        val fileBinder = ExplorerItemBinderImpl(explorerFile.root)
        val holders = arrayOf(dirBinder, fileBinder)
        val onClickListener = Listener(*holders)
        details.setOnClickListener(onClickListener)
        access.setOnClickListener(onClickListener)
        owner.setOnClickListener(onClickListener)
        group.setOnClickListener(onClickListener)
        date.setOnClickListener(onClickListener)
        time.setOnClickListener(onClickListener)
        size.setOnClickListener(onClickListener)
        box.setOnClickListener(onClickListener)
        alternating.setOnClickListener(onClickListener)

        dirBinder.bind(dir)
        fileBinder.bind(file)
        holders.bind()

        explorerDir.itemExplorerIvIcon.alpha = Alpha.VISIBLE
        explorerDir.itemExplorerTvSize.text = dir.size
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
                R.id.details -> composition.copy(visibleDetails = isChecked)
                R.id.access -> composition.copy(visibleAccess = isChecked)
                R.id.owner -> composition.copy(visibleOwner = isChecked)
                R.id.group -> composition.copy(visibleGroup = isChecked)
                R.id.date -> composition.copy(visibleDate = isChecked)
                R.id.time -> composition.copy(visibleTime = isChecked)
                R.id.size -> composition.copy(visibleSize = isChecked)
                R.id.box -> composition.copy(visibleBox = isChecked)
                R.id.alternating -> composition.copy(visibleBg = isChecked)
                else -> throw Exception()
            }
            binders.bind()
            preferenceStore { setExplorerItemComposition(composition) }
        }
    }
}