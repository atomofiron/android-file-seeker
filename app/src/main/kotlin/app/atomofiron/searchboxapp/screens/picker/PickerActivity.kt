package app.atomofiron.searchboxapp.screens.picker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import app.atomofiron.common.util.Android
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.main.MainActivity

class PickerActivity : MainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        when (val mode = intent.handle()) {
            null -> finishUrgently(intent?.action)
            else -> activityMode = mode
        }
        super.onCreate(savedInstanceState)
    }

    private fun Intent.handle(): ActivityMode? = when (intent?.action) {
        Intent.ACTION_PICK,
        Intent.ACTION_GET_CONTENT,
        Intent.ACTION_OPEN_DOCUMENT -> handleGet()
        Intent.ACTION_SEND -> handleSend()
        Intent.ACTION_SEND_MULTIPLE -> handleSendMultiple()
        else -> null
    }

    private fun Intent.handleGet() = ActivityMode.Share(
        initialUri = getUriExtra(DocumentsContract.EXTRA_INITIAL_URI),
        multiple = getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false),
        mimes = getStringArrayExtra(Intent.EXTRA_MIME_TYPES)?.toList()
            ?: type?.let { listOf(it) }
            ?: emptyList(),
    )

    private fun Intent.handleSend(): ActivityMode.Receive? {
        val subject = getStringExtra(Intent.EXTRA_SUBJECT) ?: ""
        val uris = listOfNotNull(getUriExtra(Intent.EXTRA_STREAM))
        val texts = getTextExtras()
        return when {
            uris.isEmpty() && texts.isEmpty() -> null
            else -> ActivityMode.Receive(subject, uris, texts)
        }
    }

    private fun Intent.handleSendMultiple(): ActivityMode.Receive? {
        val subject = getStringExtra(Intent.EXTRA_SUBJECT) ?: ""
        val uris = getUriExtras()
        val texts = getTextExtras()
        return when {
            uris.isEmpty() && texts.isEmpty() -> null
            else -> ActivityMode.Receive(subject, uris, texts)
        }
    }

    private fun Intent.getTextExtras(): List<CharSequence> {
        return getCharSequenceArrayListExtra(Intent.EXTRA_TEXT)
            ?: getStringArrayListExtra(Intent.EXTRA_TEXT)
            ?: getCharSequenceArrayExtra(Intent.EXTRA_TEXT)?.toList()
            ?: getStringArrayExtra(Intent.EXTRA_TEXT)?.toList()
            ?: getCharSequenceExtra(Intent.EXTRA_TEXT)?.let { listOf(it) }
            ?: getStringExtra(Intent.EXTRA_TEXT)?.let { listOf(it) }
            ?: emptyList()
    }

    private fun Intent.getUriExtra(key: String): Uri? = when {
        Android.T -> getParcelableExtra(key, Uri::class.java)
        else -> getParcelableExtra(key) as Uri?
    }

    private fun Intent.getUriExtras() = when {
        Android.T -> getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
        else -> getParcelableArrayListExtra(Intent.EXTRA_STREAM)
    }?.filterNotNull() ?: emptyList()

    private fun finishUrgently(action: String?) {
        val message = getString(R.string.wrong_intent)
        Toast.makeText(this, "$message: $action", Toast.LENGTH_LONG).show()
        finish()
    }
}
