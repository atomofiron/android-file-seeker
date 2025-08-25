package app.atomofiron.searchboxapp.screens.explorer.curtain

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainExplorerCreateBinding
import app.atomofiron.searchboxapp.custom.drawable.makeToned
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerCurtainMenuDelegate
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding

class CreateDelegate(
    private val output: ExplorerCurtainMenuDelegate,
) {

    fun getView(dir: Node, inflater: LayoutInflater): View {
        val dirFiles = dir.children?.map { it.name } ?: listOf()
        val binding = CurtainExplorerCreateBinding.inflate(inflater, null, false)
        binding.init(dir, dirFiles)
        return binding.root
    }

    private fun CurtainExplorerCreateBinding.init(dir: Node, dirFiles: List<String>) {
        root.insetsPadding(ExtType.curtain, vertical = true)
        textField.makeToned(textLayout)
        textField.text?.clear()
        textField.inputType = EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        val textListener = ButtonState(dirFiles, arrayOf(explorerCreateDirBtn, explorerCreateFileBtn))
        textField.addTextChangedListener(textListener)
        val clickListener = ButtonClick(dir, textField)
        explorerCreateDirBtn.setOnClickListener(clickListener)
        explorerCreateFileBtn.setOnClickListener(clickListener)
    }

    private inner class ButtonClick(
        private val dir: Node,
        private val editText: EditText,
    ) : View.OnClickListener {
        override fun onClick(view: View) {
            val directory = when (view.id) {
                R.id.explorer_create_dir_btn -> true
                R.id.explorer_create_file_btn -> false
                else -> throw Exception()
            }
            output.onCreateConfirm(dir, editText.text.toString(), directory)
        }
    }

    private inner class ButtonState(
        private val dirFiles: List<String>,
        private val buttons: Array<Button>,
    ) : TextWatcher {

        init {
            buttons.forEach { it.isEnabled = false }
        }

        override fun afterTextChanged(s: Editable?) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
            val allow = sequence.isNotEmpty() && !dirFiles.contains(sequence.toString())
            buttons.forEach { it.isEnabled = allow }
        }
    }
}