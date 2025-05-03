package app.atomofiron.searchboxapp.utils

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import app.atomofiron.common.util.applyIf
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.common.util.with
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.model.other.get
import com.google.android.material.dialog.MaterialAlertDialogBuilder

typealias ButtonCallback = (dontAskAgain: Boolean) -> Unit

class DialogBuilder(context: WeakProperty<out AppCompatActivity>) {

    private val context by context

    private var title: UniText? = null
    private var message: UniText? = null
    private var offerDontAskAnymore: Boolean = false
    private var positive: Pair<UniText, ButtonCallback?>? = null
    private var negative: Pair<UniText, ButtonCallback?>? = null
    private var neutral: Pair<UniText, ButtonCallback?>? = null
    private var onCancel: (() -> Unit)? = null
    private var cancelable: Boolean = true

    fun setTitle(text: String): DialogBuilder {
        title = UniText(text)
        return this
    }

    fun setTitle(@StringRes resId: Int): DialogBuilder {
        title = UniText(resId)
        return this
    }

    fun setMessage(text: String): DialogBuilder {
        message = UniText(text)
        offerDontAskAnymore = false
        return this
    }

    fun setMessage(@StringRes resId: Int): DialogBuilder {
        message = UniText(resId)
        offerDontAskAnymore = false
        return this
    }

    fun setMessageWithOfferDontAskAnymore(offer: Boolean): DialogBuilder {
        message = null
        offerDontAskAnymore = offer
        return this
    }

    fun setPositiveButton(title: String, onClick: ButtonCallback? = null): DialogBuilder {
        positive = UniText(title) to onClick
        return this
    }

    fun setPositiveButton(@StringRes resId: Int, onClick: ButtonCallback? = null): DialogBuilder {
        positive = UniText(resId) to onClick
        return this
    }

    fun setNegativeButton(title: String, onClick: ButtonCallback? = null): DialogBuilder {
        negative = UniText(title) to onClick
        return this
    }

    fun setNegativeButton(@StringRes resId: Int, onClick: ButtonCallback? = null): DialogBuilder {
        negative = UniText(resId) to onClick
        return this
    }

    fun setNeutralButton(title: String, onClick: ButtonCallback? = null): DialogBuilder {
        neutral = UniText(title) to onClick
        return this
    }

    fun setNeutralButton(@StringRes resId: Int, onClick: ButtonCallback? = null): DialogBuilder {
        neutral = UniText(resId) to onClick
        return this
    }

    fun setCancelable(cancelable: Boolean): DialogBuilder {
        this.cancelable = cancelable
        return this
    }

    fun onCancel(callback: (() -> Unit)?): DialogBuilder {
        onCancel = callback
        return this
    }

    fun show() {
        val context = context ?: return
        var dontAskAgain = false
        MaterialAlertDialogBuilder(context)
            .setCancelable(cancelable)
            .with(onCancel) { callback -> setOnCancelListener { callback() } }
            .setTitle(context.resources[title])
            .setMessage(context.resources[message])
            .applyIf(offerDontAskAnymore) {
                setMultiChoiceItems(R.array.dont_ask_anymore, null) { _, _, checked ->
                    dontAskAgain = checked
                }
            }.with(positive) { (label, onClick) ->
                setPositiveButton(context.resources[label]) { _, _ -> onClick?.invoke(dontAskAgain) }
            }.with(negative) { (label, onClick) ->
                setNegativeButton(context.resources[label]) { _, _ -> onClick?.invoke(dontAskAgain) }
            }.with(neutral) { (label, onClick) ->
                setNeutralButton(context.resources[label]) { _, _ -> onClick?.invoke(dontAskAgain) }
            }.show()
    }
}