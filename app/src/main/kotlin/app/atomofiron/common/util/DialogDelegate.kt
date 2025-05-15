package app.atomofiron.common.util

import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import app.atomofiron.common.util.extension.copy
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.DialogCheckboxBinding
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.model.other.UniText.Fmt
import app.atomofiron.searchboxapp.model.other.UniText.Res
import app.atomofiron.searchboxapp.model.other.UniText.Str
import app.atomofiron.searchboxapp.model.other.get
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogDelegate(activity: WeakProperty<out FragmentActivity>) : DialogMaker {

    private val activity by activity

    override operator fun get(text: UniText) = when (text) {
        is Fmt -> activity?.resources?.getString(text.res, *text.args).toString()
        is Res -> activity?.resources?.getString(text.value).toString()
        is Str -> text.value
    }

    override fun showError(message: String) = show(
        title = UniText(R.string.error),
        message = UniText(message),
        neutral = UniText(R.string.copy) to {
            val activity = activity
            activity?.getSystemService(Context.CLIPBOARD_SERVICE)
                ?.let { it as ClipboardManager }
                ?.copy(activity, label = activity.resources.getString(R.string.error), text = message, activity.resources)
        },
    )

    override fun show(
        icon: Drawable?,
        title: UniText?,
        message: UniText?,
        withCheckbox: DialogMaker.CheckBox?,
        cancelable: Boolean,
        onCancel: (() -> Unit)?,
        onDismiss: (() -> Unit)?,
        neutral: DialogMakerButton?,
        negative: DialogMakerButton?,
        positive: UniText,
        onPositiveClick: (checked: Boolean) -> Unit,
    ) {
        val activity = activity ?: return
        val resources = activity.resources
        var checked = false
        val messageText = resources[message]
        MaterialAlertDialogBuilder(activity)
            .setIcon(icon)
            .setCancelable(cancelable)
            .setTitle(resources[title])
            .setMessage(messageText)
            .withNotNull(onCancel) { setOnCancelListener { it() } }
            .withNotNull(onDismiss) { setOnDismissListener { it() } }
            .setPositiveButton(resources[positive]) { _, _ -> onPositiveClick.invoke(checked) }
            .withNotNull(negative) { (label, onClick) ->
                setNegativeButton(resources[label]) { _, _ -> onClick.invoke(checked) }
            }.withNotNull(neutral) { (label, onClick) ->
                setNeutralButton(resources[label]) { _, _ -> onClick.invoke(checked) }
            }.withNotNull(withCheckbox) {
                DialogCheckboxBinding.inflate(LayoutInflater.from(activity)).run {
                    root.setOnClickListener { checkbox.toggle() }
                    checkbox.setText(it.label)
                    checkbox.setOnCheckedChangeListener { _, isChecked ->
                        checked = isChecked
                    }
                    setView(root)
                }
            }.show()
    }
}