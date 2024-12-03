package app.atomofiron.searchboxapp.custom.preference

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.drawable.NoticeableDrawable
import app.atomofiron.searchboxapp.utils.getAttr
import app.atomofiron.searchboxapp.model.other.AppUpdateState

class AppUpdatePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleRes: Int = context.getAttr(
        androidx.preference.R.attr.preferenceStyle,
        android.R.attr.preferenceStyle,
    )
) : Preference(context, attrs, defStyleRes) {
    private var state: AppUpdateState = AppUpdateState.Unknown
    private val errorColor = ContextCompat.getColor(context, R.color.error)
    private val withButton: Boolean get() = state in arrayOf(AppUpdateState.Available, AppUpdateState.Downloaded)
    private var button: Button? = null
    private val buttonClickListener = ::onButtonClick

    init {
        widgetLayoutResource = R.layout.widget_button
        isSelectable = false
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        button = holder.itemView.findViewById(R.id.widgetButton)
        bindButton()
    }

    fun bind(state: AppUpdateState) {
        this.state = state

        icon?.colorFilter = when (state) {
            is AppUpdateState.Error -> PorterDuffColorFilter(errorColor, PorterDuff.Mode.SRC_IN)
            else -> null
        }

        when (state) {
            is AppUpdateState.Available -> {
                val drawable = NoticeableDrawable(context, R.drawable.ic_new)
                drawable.forceShowDot(true)
                icon = drawable
                setTitle(R.string.update_available)
            }
            is AppUpdateState.Error -> {
                setIcon(R.drawable.ic_error)
                setTitle(R.string.failed_check_updates)
            }
            is AppUpdateState.InProgress -> {
                setIcon(R.drawable.ic_progress_download)
                setTitle(R.string.update_loading)
            }
            is AppUpdateState.Downloaded -> {
                val drawable = NoticeableDrawable(context, R.drawable.ic_new)
                drawable.forceShowDot(true)
                icon = drawable
                setTitle(R.string.update_downloaded)
            }
            is AppUpdateState.Unknown,
            is AppUpdateState.UpToDate -> isVisible = false
        }
        bindButton()
    }

    private fun bindButton() {
        button?.setOnClickListener(buttonClickListener)
        button?.isVisible = withButton
        when (state) {
            is AppUpdateState.Available -> button?.setText(R.string.get_update)
            is AppUpdateState.Downloaded -> button?.setText(R.string.install)
            else -> Unit
        }
    }

    private fun onButtonClick(v: View) {
        onPreferenceClickListener?.onPreferenceClick(this)
    }
}