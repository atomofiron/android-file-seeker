package app.atomofiron.searchboxapp.screens.result.presenter

import android.os.Bundle
import app.atomofiron.searchboxapp.screens.result.ResultFragment
import app.atomofiron.searchboxapp.utils.Const

class ResultPresenterParams(
    val taskId: Int,
) {
    companion object {
        private const val KEY_TASK_ID = "KEY_TASK_ID"

        fun arguments(taskId: Int) = Bundle().apply {
            putInt(KEY_TASK_ID, taskId)
        }

        fun params(arguments: Bundle) = ResultPresenterParams(
            arguments.getInt(KEY_TASK_ID, Const.UNDEFINED)
        )
    }
}