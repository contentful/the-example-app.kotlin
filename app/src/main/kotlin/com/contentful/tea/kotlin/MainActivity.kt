package com.contentful.tea.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDAEntry
import com.contentful.tea.kotlin.data.Course
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.launch

class MainActivity : AppCompatActivity() {
    companion object {
        val LOG_TAG: String = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val client = CDAClient.builder()
            .setToken(BuildConfig.CONTENTFUL_DELIVERY_TOKEN)
            .setSpace(BuildConfig.CONTENTFUL_SPACE_ID)
            .build()

        launch {
            try {
                val courses = client
                    .fetch(CDAEntry::class.java)
                    .withContentType("course")
                    .all()
                    .items()
                    .map { Course(it as CDAEntry, "en-US") }

                Log.d(LOG_TAG, "Found ${courses.size} entries.")

                runOnUiThread {
                    val titles =
                        courses.joinToString("") {
                            "<p><b>${it.title}</b><br/>${it.shortDescription}</p>"
                        }
                    test_text.text = Html.fromHtml(titles, Html.FROM_HTML_MODE_LEGACY)

                    showInformation(
                        title = "Courses found",
                        message = courses.joinToString { it.slug })
                }
            } catch (throwable: Throwable) {
                showError(
                    message = throwable.toString(),
                    error = throwable,
                    moreTitle = "Copy to clipboard",
                    moreHandler = {
                        saveToClipboard("label", "${throwable.message} ${throwable.stackTraceText}")
                        toast("Copied to clipboard")
                    }
                )
            }
        }
    }
}
