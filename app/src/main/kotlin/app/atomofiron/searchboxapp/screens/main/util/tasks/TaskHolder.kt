package app.atomofiron.searchboxapp.screens.main.util.tasks

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.transition.TransitionManager
import app.atomofiron.common.recycler.GeneralHolder
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import app.atomofiron.fileseeker.R

class TaskHolder(itemView: View) : GeneralHolder<XTask>(itemView) {
    companion object {
        private const val CONFIRM_DELAY = 1024L
    }
    private val ivIcon: ImageView = itemView.findViewById(R.id.task_iv_icon)
    private val mtvTitle: MaterialTextView = itemView.findViewById(R.id.task_mtv_title)
    private val ibCancel: ImageButton = itemView.findViewById(R.id.task_ib_cancel)
    private val btnConfirm: MaterialButton = itemView.findViewById(R.id.task_btn_confirm)

    init {
        itemView as ViewGroup

        ibCancel.setOnClickListener { view ->
            toggle(confirm = true)
            val position = absoluteAdapterPosition
            view.postDelayed(block@{
                if (absoluteAdapterPosition != position) return@block
                toggle(confirm = false)
            }, CONFIRM_DELAY)
        }

        itemView.setOnClickListener {
        }
    }

    private fun toggle(confirm: Boolean = false) {
        if (ibCancel.isEnabled == !confirm) {
            return
        }
        TransitionManager.beginDelayedTransition(itemView as ViewGroup)
        ibCancel.isEnabled = !confirm
        btnConfirm.isEnabled = confirm
        ibCancel.layoutParams.width = if (confirm) 0 else ViewGroup.LayoutParams.WRAP_CONTENT
        btnConfirm.layoutParams.width = if (confirm) ViewGroup.LayoutParams.WRAP_CONTENT else 0
        ibCancel.layoutParams = ibCancel.layoutParams
        btnConfirm.layoutParams = btnConfirm.layoutParams
    }

    override fun onBind(item: XTask, position: Int) {
        TransitionManager.endTransitions(itemView as ViewGroup)
        ivIcon.setImageResource(R.drawable.ic_trashbox)
        mtvTitle.text = "Task $position"
        toggle()
    }
}