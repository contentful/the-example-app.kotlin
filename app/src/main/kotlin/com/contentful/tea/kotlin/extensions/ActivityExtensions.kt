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

fun Activity.showError(
    message: CharSequence,
    title: CharSequence = "Error",
    moreTitle: CharSequence = "More …",
    error: Throwable? = null,
    moreHandler: Function0<Unit>? = null,
    cancelHandler: Function0<Unit>? = null,
    okHandler: Function0<Unit> = {}
) {
    runOnUiThread {
        AlertDialog
            .Builder(this)
            .apply {
                setTitle(title)
                if (error != null) {
                    if (message.isEmpty()) {
                        setMessage(
                            Html.fromHtml(
                                "<br/><hr/><small><tt>${error.message}<tt></small>",
                                0
                            )
                        )
                    } else {
                        setMessage(
                            Html.fromHtml(
                                "$message<br><hr/><tt><small>${error.message}</small><tt>",
                                0
                            )
                        )
                    }
                } else {
                    setMessage(Html.fromHtml(message.toString(), 0))
                }
                if (moreHandler != null) {
                    setNeutralButton(moreTitle) { _, _ -> moreHandler() }
                }
                if (cancelHandler != null) {
                    setNegativeButton(moreTitle) { _, _ -> cancelHandler() }
                }
                setPositiveButton(android.R.string.ok) { _, _ -> okHandler() }
                setOnCancelListener { okHandler() }
            }
            .show()

        if (error === null) {
            Log.i(this.javaClass.simpleName, Html.fromHtml(message.toString(), 0).toString())
        } else {
            Log.e(this.javaClass.simpleName, Html.fromHtml(message.toString(), 0).toString(), error)
        }
    }
}

fun Activity.showInformation(
    message: CharSequence,
    title: CharSequence = "Information",
    moreHandler: Function0<Unit>? = null,
    moreTitle: CharSequence = "More …",
    cancelHandler: Function0<Unit>? = null,
    okHandler: Function0<Unit> = {}
) {
    showError(message, title, moreTitle, null, moreHandler, cancelHandler, okHandler)
}

val Throwable.stackTraceText: CharSequence
    get() = this.stackTrace.reversed().joinToString("<br/>")

fun Context.saveToClipboard(label: CharSequence, content: CharSequence) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipDescription = ClipDescription(label, arrayOf("text/plain"))
    val clipItem = ClipData.Item(content)
    val data = ClipData(clipDescription, clipItem)
    clipboard.primaryClip = data
}

fun Context.toast(message: CharSequence) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}