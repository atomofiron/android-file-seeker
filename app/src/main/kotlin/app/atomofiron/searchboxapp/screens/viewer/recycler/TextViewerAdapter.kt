package app.atomofiron.searchboxapp.screens.viewer.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.fileseeker.databinding.ItemTextLineBinding
import app.atomofiron.searchboxapp.model.textviewer.MatchMap
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.screens.viewer.state.MatchCursor
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.postToPosition

class TextViewerAdapter : GeneralAdapter<TextLine, TextViewerHolder>() {

    var textViewerListener: TextViewerListener? = null
    private var matches: MatchMap = mapOf()
    private var cursor: MatchCursor? = null
    private var recyclerView: RecyclerView? = null

    init {
        setHasStableIds(true)
    }

    fun setMatches(items: MatchMap?) {
        matches = items ?: mapOf()
        cursor = null
        notifyDataSetChanged()
    }

    fun setCursor(cursor: MatchCursor?) {
        val was = this.cursor?.lineIndex
        this.cursor = cursor
        if (was != null) {
            notifyItemChanged(was)
        }
        if (cursor != null && cursor.lineIndex >= 0 && cursor.lineIndex != was) {
            notifyItemChanged(cursor.lineIndex)
        }
        cursor?.lineIndex?.let {
            recyclerView?.postToPosition(it)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): TextViewerHolder {
        val binding = ItemTextLineBinding.inflate(inflater, parent, false)
        return TextViewerHolder(binding.root)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: TextViewerHolder, position: Int) {
        val cursor = cursor
        val indexFocus = when {
            cursor == null -> Const.UNDEFINED
            position == cursor.lineIndex -> cursor.matchIndex
            else -> Const.UNDEFINED
        }
        val matches = matches[position]
        when {
            matches.isNullOrEmpty() -> holder.bind(items[position], position)
            else -> holder.bindMatches(items[position], position, matches, indexFocus)
        }
        textViewerListener?.onLineVisible(position)
    }

    interface TextViewerListener {
        fun onLineVisible(index: Int)
    }
}