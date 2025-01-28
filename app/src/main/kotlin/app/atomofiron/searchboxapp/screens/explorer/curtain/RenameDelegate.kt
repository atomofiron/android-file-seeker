package app.atomofiron.searchboxapp.screens.explorer.curtain

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import app.atomofiron.fileseeker.databinding.CurtainExplorerRenameBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerCurtainMenuDelegate
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding

class RenameDelegate(
    private val output: ExplorerCurtainMenuDelegate,
) {

    var data: RenameData? = null

    fun getView(data: RenameData, inflater: LayoutInflater): View {
        val binding = CurtainExplorerRenameBinding.inflate(inflater, null, false)
        binding.init(data)
        return binding.root
    }

    private fun CurtainExplorerRenameBinding.init(data: RenameData) {
        root.insetsPadding(ExtType.curtain, vertical = true)
        val holder = ExplorerHolder(explorerRenameItem.root)
        holder.bind(data.item)
        holder.bindComposition(data.composition)
        holder.disableClicks()
        holder.setGreyBackgroundColor()
        holder.hideCheckBox()
        explorerRenameEt.inputType = EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        explorerRenameEt.setText(data.item.name)
        explorerRenameEt.addTextChangedListener(ButtonClick(data, explorerRenameBtn))
        explorerRenameBtn.setOnClickListener {
            output.onRenameConfirm(data.item, explorerRenameEt.text.toString())
        }
        explorerRenameBtn.isEnabled = false

        val dotIndex = data.item.name.lastIndexOf('.')
        if (dotIndex > 0) {
            explorerRenameEt.setSelection(dotIndex)
        }
    }

    data class RenameData(
        val composition: ExplorerItemComposition,
        val item: Node,
        val items: List<String>
    )

    private class ButtonClick(
        private val data: RenameData,
        private val button: Button,
    ) : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
            val allow = sequence.isNotEmpty() && !data.items.contains(sequence.toString())
            button.isEnabled = allow
        }
    }
}