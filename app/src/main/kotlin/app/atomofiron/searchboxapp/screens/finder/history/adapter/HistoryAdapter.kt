package app.atomofiron.searchboxapp.screens.finder.history.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.finder.history.dao.HistoryDao
import app.atomofiron.searchboxapp.screens.finder.history.dao.HistoryDatabase
import app.atomofiron.searchboxapp.screens.finder.history.dao.ItemHistory
import app.atomofiron.searchboxapp.screens.finder.history.dao.Migrations
import app.atomofiron.searchboxapp.utils.mutate

class HistoryAdapter(
    context: Context,
    private val onItemClickListener: OnItemClickListener
) : ListAdapter<ItemHistory, HistoryHolder>(HistoryItemCallback), HistoryHolder.OnItemActionListener {
    companion object {
        private const val DB_NAME = "history"
        private const val UNDEFINED = -1
        private const val FIRST = 0
    }
    private val db = Room.databaseBuilder(context, HistoryDatabase::class.java, DB_NAME)
        .addMigrations(Migrations.MIGRATION_1_2)
        .allowMainThreadQueries()
        .build()
    private lateinit var dao: HistoryDao

    init {
        setHasStableIds(true)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        dao = db.historyDao()
        reload()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        db.close()
    }

    fun add(string: String) {
        if (string.isBlank()) {
            return
        }
        var index = currentList.indexOfFirst { !it.pinned }
        if (index == UNDEFINED) {
            index = itemCount
        }
        currentList.mutate {
            find { it.title == string }?.let {
                dao.delete(it)
                remove(it)
            }
            var item = ItemHistory(title = string)
            item = item.copy(id = dao.insert(item))
            add(index, item)
            submitList(this)
        }
    }

    fun reload(unit: Unit = Unit) = submitList(dao.all)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_history, parent, false)
        return HistoryHolder(view, this)
    }


    override fun getItemId(position: Int): Long = getItem(position).id

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        val item = getItem(position)
        holder.onBind(item.title, item.pinned)
    }

    override fun onItemClick(position: Int) {
        val item = getItem(position)
        onItemClickListener.onItemClick(item.title)
    }

    override fun onItemPin(position: Int) {
        val item = getItem(position)
        val nextPosition = if (item.pinned) currentList.indexOfLast { it.pinned } else FIRST
        dao.delete(item)
        var new = item.copy(id = 0, pinned = !item.pinned)
        new = new.copy(id = dao.insert(new))
        currentList.mutate {
            removeAt(position)
            add(nextPosition, new)
            submitList(this)
        }
    }

    override fun onItemRemove(position: Int) {
        val item = getItem(position)
        dao.delete(item)
        currentList.mutate {
            removeAt(position)
            submitList(this)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(node: String)
    }
}
