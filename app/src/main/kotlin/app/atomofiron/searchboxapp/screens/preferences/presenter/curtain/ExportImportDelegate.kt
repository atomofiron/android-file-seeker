package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.view.LayoutInflater
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainPreferenceExportImportBinding
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding

class ExportImportDelegate(
    private val output: ExportImportOutput,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainPreferenceExportImportBinding.inflate(inflater, null, false)
        binding.init()
        binding.root.insetsPadding(ExtType.curtain, vertical = true)
        return CurtainApi.ViewHolder(binding.root)
    }

    private fun CurtainPreferenceExportImportBinding.init() {
        leiRgAction.setOnCheckedChangeListener { _, checkedId ->
            val id = when (checkedId) {
                R.id.lei_rb_export -> R.string.export_btn
                R.id.lei_rb_import -> R.string.import_btn
                else -> throw IllegalArgumentException()
            }
            leiBtn.setText(id)
        }
        leiBtn.setOnClickListener { onButtonClick() }
        // todo share leiTvPath.text = output.externalPath
    }

    private fun CurtainPreferenceExportImportBinding.onButtonClick() {
        when (leiRgAction.checkedRadioButtonId) {
            R.id.lei_rb_export -> when (leiRgTarget.checkedRadioButtonId) {
                R.id.lei_rb_preferences -> output.exportPreferences()
                R.id.lei_rb_history -> output.exportHistory()
                else -> throw Exception()
            }
            R.id.lei_rb_import -> when (leiRgTarget.checkedRadioButtonId) {
                R.id.lei_rb_preferences -> output.importPreferences()
                R.id.lei_rb_history -> output.importHistory()
                else -> throw Exception()
            }
            else -> throw Exception()
        }
        controller?.close()
    }

    interface ExportImportOutput {
        fun exportPreferences()
        fun exportHistory()
        fun importPreferences()
        fun importHistory()
    }
}