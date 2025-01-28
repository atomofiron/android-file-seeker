package app.atomofiron.searchboxapp.screens.result

import android.content.Intent
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
import app.atomofiron.searchboxapp.utils.Const
import java.util.*

class ResultRouter(property: WeakProperty<out Fragment>) : BaseRouter(property) {

    override val currentDestinationId = R.id.resultFragment

    fun shareFile(title: String, data: String): Boolean {
        val intent = Intent(Intent.ACTION_SEND)
            .setType(Const.MIME_TYPE_TEXT)
            .putExtra(Intent.EXTRA_SUBJECT, title)
            .putExtra(Intent.EXTRA_TITLE, title)
            .putExtra(Intent.EXTRA_TITLE, title)
            .putExtra(Intent.EXTRA_TEXT, data)

        val activity = activity!!
        val success = intent.resolveActivity(activity.packageManager) != null
        if (success) {
            val chooser = Intent.createChooser(intent, "")
            activity.startActivity(chooser)
        }
        return success
    }

    fun openFile(path: String, taskId: UUID) {
        val arguments = TextViewerParams.arguments(path, taskId)
        navigate(R.id.textViewerFragment, arguments)
    }
}