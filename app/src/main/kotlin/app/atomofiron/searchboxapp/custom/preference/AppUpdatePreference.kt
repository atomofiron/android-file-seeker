package app.atomofiron.searchboxapp.custom.preference

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.ButtonStyle
import app.atomofiron.searchboxapp.custom.drawable.NoticeableDrawable
import app.atomofiron.searchboxapp.model.other.AppUpdateState
import app.atomofiron.searchboxapp.utils.getAttr
import com.google.android.material.progressindicator.CircularProgressIndicator

private const val MAX = 100

class AppUpdatePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleRes: Int = context.getAttr(
        androidx.preference.R.attr.preferenceStyle,
        android.R.attr.preferenceStyle,
    )
) : Preference(context, attrs, defStyleRes), View.OnClickListener {
    private var state: AppUpdateState = AppUpdateState.Unknown
    private val buttonStyle = ButtonStyle(context)
    private lateinit var button: Button
    private lateinit var progress: CircularProgressIndicator

    init {
        widgetLayoutResource = R.layout.widget_button_with_progress
        isSelectable = false
        state = AppUpdateState.Completable
        state.bindIcon()
        state.bindTitle()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        button = holder.itemView.findViewById(R.id.widgetButton)
        progress = holder.itemView.findViewById(R.id.widgetProgress)
        button.setOnClickListener(this)
        progress.max = MAX
        state.bindButton()
        state.bindProgress()
    }

    fun bind(state: AppUpdateState) {
        if (state != this.state) {
            this.state = state
            state.bindIcon()
            state.bindTitle()
        }
    }

    private fun AppUpdateState.bindIcon() {
        when (this) {
            is AppUpdateState.Available,
            is AppUpdateState.Completable -> {
                val drawable = NoticeableDrawable(context, R.drawable.ic_new)
                drawable.forceShowDot(true)
                icon = drawable
                return
            }
            is AppUpdateState.Downloading,
            is AppUpdateState.Installing -> R.drawable.ic_progress_download
            is AppUpdateState.UpToDate -> R.drawable.ic_smile
            is AppUpdateState.Unknown -> R.drawable.ic_error
        }.let { setIcon(it) }
    }

    private fun AppUpdateState.bindTitle() {
        when (this) {
            is AppUpdateState.Unknown -> R.string.check_updates
            is AppUpdateState.Available -> R.string.update_available
            is AppUpdateState.Downloading -> R.string.update_loading
            is AppUpdateState.Installing -> R.string.update_installing
            is AppUpdateState.UpToDate -> R.string.is_up_to_date
            is AppUpdateState.Completable -> R.string.install_update
        }.let { setTitle(it) }
    }

    private fun AppUpdateState.bindButton() {
        val stringId = when (this) {
            is AppUpdateState.Unknown -> R.string.check
            is AppUpdateState.Available -> R.string.download
            is AppUpdateState.Completable -> R.string.install
            is AppUpdateState.Downloading,
            is AppUpdateState.Installing,
            is AppUpdateState.UpToDate -> null
        }
        button.isVisible = stringId != null
        when {
            !button.isVisible -> return
            this is AppUpdateState.Unknown -> buttonStyle.outlined(button)
            else -> buttonStyle.filled(button)
        }
        stringId?.let { button.setText(it) }
    }

    private fun AppUpdateState.bindProgress() {
        val value = when (this) {
            is AppUpdateState.Installing -> null
            is AppUpdateState.Downloading -> progress
            is AppUpdateState.Unknown,
            is AppUpdateState.Available,
            is AppUpdateState.Completable,
            is AppUpdateState.UpToDate -> {
                progress.isVisible = false
                return
            }
        }
        progress.isVisible = true
        progress.isIndeterminate = value == null
        value?.let { progress.setProgress((MAX * value).toInt()) }
        buttonStyle
    }

    override fun onClick(v: View) {
        onPreferenceClickListener?.onPreferenceClick(this)
    }
}