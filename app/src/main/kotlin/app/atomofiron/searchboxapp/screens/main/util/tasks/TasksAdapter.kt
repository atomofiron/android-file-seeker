package app.atomofiron.searchboxapp.screens.main.util.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.fileseeker.R

class TasksAdapter : GeneralAdapter<XTask, TaskHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): TaskHolder {
        val view = inflater.inflate(R.layout.item_task, parent, false)
        return TaskHolder(view)
    }
}