package app.atomofiron.searchboxapp.screens.finder.history.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.finder.history.dao.HistoryDao
import app.atomofiron.searchboxapp.screens.finder.history.dao.HistoryDatabase
import app.atomofiron.searchboxapp.screens.finder.history.dao.ItemHistory
import app.atomofiron.searchboxapp.screens.finder.history.dao.Migrations

class HistoryAdapter(
    context: Context,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<HistoryHolder>(), HistoryHolder.OnItemActionListener {
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
    private val items = mutableListOf<ItemHistory>()

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
        var index = items.indexOfFirst { !it.pinned }
        if (index == UNDEFINED) {
            index = items.size
        }
        var item = ItemHistory(title = string)
        item = item.copy(id = dao.insert(item))
        items.add(index, item)
        notifyItemInserted(index)
    }

    fun reload(unit: Unit = Unit) {
        items.clear()
        items.addAll(dao.all)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_history, parent, false)
        return HistoryHolder(view, this)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].id

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        val item = items[position]
        holder.onBind(item.title, item.pinned)
    }

    override fun onItemClick(position: Int) {
        val item = items[position]
        onItemClickListener.onItemClick(item.title)
    }

    override fun onItemPin(position: Int) {
        val item = items[position]
        val nextPosition = if (item.pinned) items.indexOfLast { it.pinned } else FIRST
        dao.delete(item)
        var new = item.copy(id = 0, pinned = !item.pinned)
        new = new.copy(id = dao.insert(new))
        notifyItemChanged(position)

        items.removeAt(position)
        items.add(nextPosition, new)
        notifyItemMoved(position, nextPosition)
    }

    override fun onItemRemove(position: Int) {
        val item = items[position]
        dao.delete(item)
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    interface OnItemClickListener {
        fun onItemClick(node: String)
    }
}