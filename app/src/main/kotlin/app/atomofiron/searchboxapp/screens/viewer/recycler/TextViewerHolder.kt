package app.atomofiron.searchboxapp.screens.viewer.recycler

import android.text.Spannable
import android.text.SpannableString
import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.extension.debugFail
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.style.EntireLineSpan
import app.atomofiron.searchboxapp.custom.view.style.RoundedBackgroundSpan
import app.atomofiron.searchboxapp.di.dependencies.store.DefaultCharset
import app.atomofiron.searchboxapp.model.textviewer.MatchList
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.utils.colorAttr
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.nio.charset.CodingErrorAction

private val charBuf: CharBuffer = CharBuffer.allocate(1024)
private val DefaultDecoder: CharsetDecoder = DefaultCharset.decoder()

class TextViewerHolder(
    private val textView: TextView,
) : GeneralHolder<TextLine>(textView) {

    private var charset: Charset = DefaultCharset
    private var decoder: CharsetDecoder = DefaultDecoder

    private val spanPart: RoundedBackgroundSpan
        get() = RoundedBackgroundSpan(
            backgroundColor = context.colorAttr(MaterialAttr.colorSurfaceVariant),
            borderColor = context.colorAttr(MaterialAttr.colorSecondary),
            textColor = context.colorAttr(MaterialAttr.colorOnSurfaceVariant),
            context.resources.getDimension(R.dimen.background_span_corner_radius),
            context.resources.getDimension(R.dimen.background_span_border_thickness),
    )

    private val spanPartFocus: RoundedBackgroundSpan
        get() = RoundedBackgroundSpan(
            backgroundColor = context.colorAttr(MaterialAttr.colorSecondary),
            borderColor = context.colorAttr(MaterialAttr.colorSecondary),
            textColor = context.colorAttr(MaterialAttr.colorOnSecondary),
            context.resources.getDimension(R.dimen.background_span_corner_radius),
            context.resources.getDimension(R.dimen.background_span_border_thickness),
    )

    private val spanLine: EntireLineSpan
        get() = EntireLineSpan(
            context.colorAttr(MaterialAttr.colorSecondary),
            context.colorAttr(MaterialAttr.colorOnSecondary),
            context.resources.getDimension(R.dimen.background_span_corner_radius)
    )

    private val spanLineFocus: EntireLineSpan
        get() = EntireLineSpan(
            context.colorAttr(MaterialAttr.colorTertiary),
            context.colorAttr(MaterialAttr.colorOnTertiary),
            context.resources.getDimension(R.dimen.background_span_corner_radius)
    )

    override fun onBind(item: TextLine, position: Int) {
        setCharset(item.charset)
        textView.text = item.toTextString()
        // android:textIsSelectable="true" breaks down
        textView.setTextIsSelectable(true)
    }

    fun bindMatches(item: TextLine, position: Int, matches: MatchList, indexFocus: Int) {
        truePosition = position
        setCharset(item.charset)
        val text = item.toTextString()
        val bytes = text.toByteArray(DefaultCharset)
        val spannable = SpannableString(text)
        for (index in matches.indices) {
            val match = matches[index]
            val skip = item.skipBytes()
            val bytesStart = (match.offset - item.offset - skip).toInt()
            val bytesEnd = (bytesStart + match.length.toInt())
            val start = bytes.countChars(0, bytesStart)
            if (bytesStart < 0 || bytesEnd > bytes.size || bytesStart > bytesEnd) {
                debugFail { "$bytesStart < 0 || $bytesEnd > ${bytes.size} || $bytesStart > $bytesEnd, text $text" }
                continue
            }
            val length = bytes.countChars(bytesStart, bytesEnd)
            val end = start + length
            val forTheEntireLine = start == 0 && end == spannable.length
            val span: Any = when {
                forTheEntireLine && index == indexFocus -> spanLineFocus
                forTheEntireLine -> spanLine
                index == indexFocus -> spanPartFocus
                else -> spanPart
            }
            when {
                start < 0 || end > spannable.length -> debugFail { "$start < 0 || $end > ${spannable.length}, spannable $spannable" }
                else -> spannable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        textView.text = spannable
        // android:textIsSelectable="true" breaks down
        textView.setTextIsSelectable(true)
    }

    private fun setCharset(charset: Charset) {
        if (charset != this.charset) {
            this.charset = charset
            decoder = charset.decoder()
        }
    }

    private fun TextLine.toTextString() = String(text, skip, text.size - skip, charset)

    private fun TextLine.skipBytes(): ULong = when (skip) {
        0 -> 0uL
        else -> String(text, 0, skip, charset)
            .toByteArray(DefaultCharset)
            .size.toULong()
    }
}

private fun ByteArray.countChars(start: Int, end: Int): Int {
    var count = 0
    val byteBuf = ByteBuffer.wrap(this, start, end - start)

    while (true) {
        val result = DefaultDecoder.decode(byteBuf, charBuf, true)
        count += charBuf.position()
        charBuf.clear()
        when {
            result.isUnderflow -> break
            result.isOverflow -> continue
            result.isError -> result.throwException()
        }
    }
    val result = DefaultDecoder.flush(charBuf)
    count += charBuf.position()
    charBuf.clear()
    DefaultDecoder.reset()

    if (result.isError) {
        result.throwException()
    }
    return count
}

private fun Charset.decoder() = newDecoder()
    .onMalformedInput(CodingErrorAction.REPLACE)
    .onUnmappableCharacter(CodingErrorAction.REPLACE)
