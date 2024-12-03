package app.atomofiron.searchboxapp.screens.viewer.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.screens.viewer.TextViewerViewState.MatchCursor

class TextViewerAdapter : GeneralAdapter<TextViewerHolder, TextLine>() {
    var textViewerListener: TextViewerListener? = null
    private var matches: Map<Int, List<TextLineMatch>> = mapOf()

    private var cursor = MatchCursor()

    private var recyclerView: RecyclerView? = null

    init {
        setHasStableIds(true)
    }

    fun setMatches(items: Map<Int, List<TextLineMatch>>?) {
        matches = items ?: mapOf()
        cursor = MatchCursor()
        notifyDataSetChanged()
    }

    fun setCursor(cursor: MatchCursor) {
        if (this.cursor.lineIndex > 0) {
            notifyItemChanged(this.cursor.lineIndex)
        }
        this.cursor = cursor

        if (cursor.lineIndex > 0) {
            notifyItemChanged(cursor.lineIndex)
            recyclerView?.post {
                recyclerView?.scrollToPosition(cursor.lineIndex)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): TextViewerHolder {
        val textView = TextView(parent.context)
        textView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        return TextViewerHolder(textView)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: TextViewerHolder, position: Int) {
        val indexFocus = when (position) {
            cursor.lineIndex -> cursor.lineMatchIndex
            else -> null
        }
        holder.onBind(items[position], matches[position], indexFocus)
        textViewerListener?.onLineVisible(position)
    }

    interface TextViewerListener {
        fun onLineVisible(index: Int)
    }
}