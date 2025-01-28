package app.atomofiron.searchboxapp.screens.preferences.presenter.curtain

import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import app.atomofiron.common.util.RadioGroupImpl
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.CurtainPreferenceToyboxBinding
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.Shell
import lib.atomofiron.insets.insetsPadding
import java.io.File

class ToyboxDelegate(
    private val preferenceStore: PreferenceStore,
) : CurtainApi.Adapter<CurtainApi.ViewHolder>() {

    private val radioGroup = RadioGroupImpl()

    private var variant: String
    private var customPath: String

    init {
        val toyboxVariant = preferenceStore.toyboxVariant.value
        variant = toyboxVariant.variant
        customPath = toyboxVariant.customPath
    }

    override fun getHolder(inflater: LayoutInflater, layoutId: Int): CurtainApi.ViewHolder {
        val binding = CurtainPreferenceToyboxBinding.inflate(inflater, null, false)
        binding.init()
        binding.root.insetsPadding(ExtType.curtain, vertical = true)
        return CurtainApi.ViewHolder(binding.root)
    }

    private fun CurtainPreferenceToyboxBinding.init() {
        preferenceEtPathToToybox.setText(customPath)

        when (variant) {
            Const.VALUE_TOYBOX_X86_64 -> preferenceRbToyboxX8664.isChecked = true
            Const.VALUE_TOYBOX_ARM_32 -> preferenceRbToybox32.isChecked = true
            Const.VALUE_TOYBOX_ARM_64 -> preferenceRbToybox64.isChecked = true
            else -> preferenceRbPathToToybox.isChecked = true
        }
        radioGroup.clear()
            .syncWith(preferenceRbPathToToybox)
            .syncWith(preferenceRbToybox64)
            .syncWith(preferenceRbToybox32)
            .syncWith(preferenceRbToyboxX8664)
        radioGroup.onCheckedChangeListener = {
            onPathChanged()
        }

        preferenceEtPathToToybox.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                preferenceRbPathToToybox.isChecked = true
                view.clearFocus()
                onPathChanged()
            }
            false
        }
    }

    private fun CurtainPreferenceToyboxBinding.onPathChanged() {
        variant = when (radioGroup.checkedId) {
            R.id.preference_rb_toybox_x86_64 -> Const.VALUE_TOYBOX_X86_64
            R.id.preference_rb_toybox_32 -> Const.VALUE_TOYBOX_ARM_32
            R.id.preference_rb_toybox_64 -> Const.VALUE_TOYBOX_ARM_64
            else -> Const.VALUE_TOYBOX_CUSTOM
        }
        var cpOutput: Shell.Output? = null
        var importedPath: String? = null
        if (variant == Const.VALUE_TOYBOX_CUSTOM) {
            customPath = preferenceEtPathToToybox.text.toString()
            importedPath = ToyboxVariant.getToyboxPath(root.context, Const.VALUE_TOYBOX_IMPORTED)

            if (!File(customPath).canExecute()) {
                val cp = Shell[Shell.CP_F, Const.EMPTY].format(customPath, importedPath)
                cpOutput = Shell.exec(cp, su = false)
                File(importedPath).setExecutable(true, true)
                customPath = importedPath
            }
        }
        when {
            cpOutput == null && test() -> preferenceStore { setToyboxVariant(setOf(variant, customPath)) }
            cpOutput?.success == true && test() -> preferenceStore { setToyboxVariant(setOf(variant, importedPath!!)) }
            cpOutput?.success == false -> controller?.showSnackbar(cpOutput.error, Snackbar.LENGTH_LONG)
        }
    }

    private fun test(): Boolean {
        val version = Shell[Shell.VERSION, customPath]
        val output = Shell.exec(version, su = false)
        val works = output.error.isBlank()
        when {
            works -> controller?.showSnackbar(output.output.trim(), Snackbar.LENGTH_LONG)
            else -> controller?.showSnackbar(output.error, Snackbar.LENGTH_LONG)
        }
        return works
    }
}