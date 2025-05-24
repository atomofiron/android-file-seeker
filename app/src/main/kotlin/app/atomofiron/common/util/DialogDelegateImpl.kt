package app.atomofiron.common.util

import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import app.atomofiron.common.util.extension.copy
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.DialogCheckboxBinding
import app.atomofiron.searchboxapp.custom.drawable.BallsDrawable
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.model.other.UniText.Fmt
import app.atomofiron.searchboxapp.model.other.UniText.Res
import app.atomofiron.searchboxapp.model.other.UniText.Str
import app.atomofiron.searchboxapp.model.other.get
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.LazyThreadSafetyMode.NONE

private const val STUB = "stub"

class DialogDelegateImpl(activity: WeakProperty<out FragmentActivity>) : DialogMaker {

    private val activity by activity
    private val resources get() = activity?.resources

    override fun loadingIcon(): Drawable? = activity?.let { BallsDrawable(it) }

    override operator fun get(text: UniText) = when (text) {
        is Fmt -> activity?.resources?.getString(text.res, *text.args).toString()
        is Res -> activity?.resources?.getString(text.value).toString()
        is Str -> text.value
    }

    override fun showError(message: UniText?) {
        val text = resources?.get(message)
        show(errorDialogConfig(text, ::copy))
    }

    override fun show(config: DialogConfig): DialogUpdater? {
        val activity = activity ?: return null
        val inflater = LayoutInflater.from(activity)
        val resources = activity.resources
        return MaterialAlertDialogBuilder(activity)
            .setCancelable(false)
            // don't remove views on the build
            .setTitle(STUB)
            .setMessage(STUB)
            .setPositiveButton(STUB, null)
            .setNegativeButton(STUB, null)
            .setNeutralButton(STUB, null)
            .show()
            .let { dialog -> UpdaterImpl(inflater, resources, dialog, config, ::copy) }
    }

    private fun copy(text: String) {
        val activity = activity
        activity?.getSystemService(Context.CLIPBOARD_SERVICE)
            ?.let { it as ClipboardManager }
            ?.copy(activity, label = activity.resources.getString(R.string.error), text = text, activity.resources)
    }

    private class UpdaterImpl(
        inflater: LayoutInflater,
        private val resources: Resources,
        private val dialog: AlertDialog,
        private var config: DialogConfig,
        private var onCopyClick: (text: String) -> Unit,
    ) : DialogUpdater {

        private var checked = false
        private val checkboxLayout by lazy(NONE) {
            DialogCheckboxBinding.inflate(inflater).apply {
                root.setOnClickListener { checkbox.toggle() }
                checkbox.setOnCheckedChangeListener { _, isChecked -> checked = isChecked }
            }
        }

        init {
            update()
            dialog.run {
                setOnCancelListener { config.onCancel?.invoke() }
                setOnDismissListener { config.onDismiss?.invoke() }
            }
        }

        override fun update(action: DialogConfig.() -> DialogConfig) = dialog.run {
            config = config.action()
            update()
        }

        override fun showError(message: String?) = update {
            errorDialogConfig(message, onCopyClick)
        }

        private fun update() = dialog.run {
            setCancelable(config.cancelable)
            setIcon(config.icon)
            setTitle(resources[config.title])
            setMessage(resources[config.message])
            getButton(AlertDialog.BUTTON_POSITIVE).run {
                text = resources[config.positive]
                setButton(AlertDialog.BUTTON_POSITIVE, null) { _, _ ->
                    config.onPositiveClick(checked)
                }
            }
            getButton(AlertDialog.BUTTON_NEGATIVE).run {
                isVisible = config.negative != null
                withNotNull(config.negative) { (label, onClick) ->
                    text = resources[label]
                    setButton(AlertDialog.BUTTON_NEGATIVE, null) { _, _ -> onClick(checked) }
                }
            }
            getButton(AlertDialog.BUTTON_NEUTRAL).run {
                isVisible = config.neutral != null
                withNotNull(config.neutral) { (label, onClick) ->
                    text = resources[label]
                    setButton(AlertDialog.BUTTON_NEUTRAL, null) { _, _ -> onClick(checked) }
                }
            }
            config.withCheckbox?.let {
                checkboxLayout.checkbox.setText(it.label)
                dialog.setView(checkboxLayout.root)
            } ?: dialog.setView(null)
        }
    }
}