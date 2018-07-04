package com.contentful.tea.kotlin.extensions

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

fun Activity.showError(
    message: String,
    title: String = "Error",
    error: Throwable? = null,
    moreHandler: Function0<Unit>? = null,
    moreTitle: String = "More …",
    cancelHandler: Function0<Unit>? = null,
    okHandler: Function0<Unit> = {}
) {
    runOnUiThread {
        AlertDialog
            .Builder(this)
            .apply {
                setTitle(title)
                if (error != null) {
                    setMessage("$message\n\nStacktrace: ${error.stackTraceText}")
                } else {
                    setMessage(message)
                }
                if (moreHandler != null) {
                    setNeutralButton(moreTitle) { _, _ -> moreHandler() }
                }
                if (cancelHandler != null) {
                    setNegativeButton(moreTitle) { _, _ -> cancelHandler() }
                }
                setPositiveButton(android.R.string.ok) { _, _ -> okHandler() }
            }
            .show()

        if (error === null) {
            Log.i(this.javaClass.simpleName, message)
        } else {
            Log.e(this.javaClass.simpleName, message, error)
        }
    }
}

fun Activity.showInformation(
    message: String,
    title: String = "Information",
    moreHandler: Function0<Unit>? = null,
    moreTitle: String = "More …",
    cancelHandler: Function0<Unit>? = null,
    okHandler: Function0<Unit> = {}
) {
    showError(message, title, null, moreHandler, moreTitle, cancelHandler, okHandler)
}

val Throwable.stackTraceText: String
    get() = this.stackTrace.reversed().joinToString("\n")

fun Context.saveToClipboard(label: String, content: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipDescription = ClipDescription(label, arrayOf("text/plain"))
    val clipItem = ClipData.Item(content)
    val data = ClipData(clipDescription, clipItem)
    clipboard.primaryClip = data
}

fun Context.toast(message: CharSequence) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}