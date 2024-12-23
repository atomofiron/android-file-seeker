package app.atomofiron.searchboxapp.screens.explorer.curtain

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import app.atomofiron.searchboxapp.databinding.CurtainExplorerCloneBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerCurtainMenuDelegate
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding

class CloneDelegate(
    private val output: ExplorerCurtainMenuDelegate,
) {

    fun getView(parent: Node, target: Node, inflater: LayoutInflater): View {
        val dirFiles = parent.children?.map { it.name } ?: listOf()
        val binding = CurtainExplorerCloneBinding.inflate(inflater, null, false)
        binding.init(target, dirFiles)
        return binding.root
    }

    private fun CurtainExplorerCloneBinding.init(target: Node, dirFiles: List<String>) {
        root.insetsPadding(ExtType.curtain, vertical = true)
        input.setText(target.name)
        val textListener = ButtonState(dirFiles, submit)
        input.addTextChangedListener(textListener)
        submit.setOnClickListener {
            output.onCloneConfirm(target, input.text.toString())
        }
    }

    private inner class ButtonState(
        private val dirFiles: List<String>,
        private val button: Button,
    ) : TextWatcher {

        init {
            button.isEnabled = false
        }

        override fun afterTextChanged(s: Editable?) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
            val allow = sequence.isNotEmpty() && !dirFiles.contains(sequence.toString())
            button.isEnabled = allow
        }
    }
}