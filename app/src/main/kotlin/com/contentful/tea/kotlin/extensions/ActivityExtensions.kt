package com.contentful.tea.kotlin.extensions

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.text.Html
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.contentful.tea.kotlin.R

fun Activity.showError(
    message: CharSequence,
    title: CharSequence = "Error occurred",
    moreTitle: CharSequence = "More …",
    error: Throwable? = null,
    moreHandler: Function0<Unit>? = null,
    cancelHandler: Function0<Unit>? = null,
    okHandler: Function0<Unit> = {}
) {
    runOnUiThread {
        AlertDialog
            .Builder(this, R.style.TeaErrorDialog)
            .apply {
                setTitle(title)
                setMessage(extractMessage(error, message))
                if (moreHandler != null) {
                    setNeutralButton(moreTitle) { _, _ -> moreHandler() }
                }
                if (cancelHandler != null) {
                    setNegativeButton(android.R.string.cancel) { _, _ -> cancelHandler() }
                }
                setPositiveButton(android.R.string.ok) { _, _ -> okHandler() }
                setOnCancelListener { okHandler() }
            }
            .show()

        if (error === null) {
            Log.i(this.javaClass.simpleName, message.removeHtmlTags().replace('\n', ' '))
        } else {
            Log.e(this.javaClass.simpleName, message.removeHtmlTags().replace('\n', ' '), error)
        }
    }
}

fun Activity.showNetworkError() {
    showError(
        message = getString(R.string.error_no_internet_connection)
    )
}

private fun extractMessage(error: Throwable?, message: CharSequence): CharSequence =
    if (error != null) {
        if (message.isEmpty()) {
            if (error.message.isNullOrEmpty()) {
                "🛑"
            } else {
                "<small><tt>${error.message}<tt></small>".toHtml()
            }
        } else {
            if (error.message.isNullOrEmpty()) {
                message.toHtml()
            } else {
                "$message<br><hr/><tt><small>${error.message}</small><tt>".toHtml()
            }
        }
    } else {
        message.toHtml()
    }

fun CharSequence.toHtml(): CharSequence = Html.fromHtml(toString(), 0)
fun CharSequence.removeHtmlTags(): String = Html.fromHtml(toString(), 0).toString()

fun Context.saveToClipboard(label: CharSequence, content: CharSequence) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipDescription = ClipDescription(label, arrayOf("text/plain"))
    val clipItem = ClipData.Item(content)
    val data = ClipData(clipDescription, clipItem)
    clipboard.primaryClip = data
}

fun Activity.toast(message: CharSequence, long: Boolean = true) = this.runOnUiThread {
    val length = if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, message.toHtml(), length).show()
}