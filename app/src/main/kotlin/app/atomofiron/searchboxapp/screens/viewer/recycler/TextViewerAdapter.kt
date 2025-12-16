package app.atomofiron.searchboxapp.screens.viewer.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.textviewer.MatchMap
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.screens.viewer.TextViewerViewState.MatchCursor
import app.atomofiron.searchboxapp.utils.Const

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
        val padding = parent.resources.getDimensionPixelSize(R.dimen.content_margin_half)
        textView.updatePaddingRelative(start = padding, end = padding)
        return TextViewerHolder(textView)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: TextViewerHolder, position: Int) {
        val cursor = cursor
        val indexFocus = when {
            cursor == null -> Const.UNDEFINED
            position == cursor.lineIndex -> cursor.lineMatchIndex
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