package com.contentful.tea.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDAEntry
import com.contentful.tea.kotlin.data.Course
import com.contentful.tea.kotlin.data.LessonModule
import kotlinx.android.synthetic.main.activity_main.*
import com.m2mobi.markymarkandroid.MarkyMarkAndroid
import com.m2mobi.markymarkcontentful.ContentfulFlavor
import kotlinx.coroutines.experimental.launch

class MainActivity : AppCompatActivity() {
    companion object {
        val LOG_TAG: String = MainActivity::class.java.simpleName
    }

    @SuppressWarnings("SetTextI18n") // TODO: Remove me once placeholder UI is gone
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
                    .include(10)
                    .all()
                    .entries()
                    .values
                    .mapNotNull {
                        if (it.contentType().id() == "course")
                            Course(it, "en-US")
                        else
                            null
                    }

                Log.d(LOG_TAG, "Found ${courses.size} entries.")

                runOnUiThread {
                    val markyMark = MarkyMarkAndroid.getMarkyMark(
                        applicationContext,
                        ContentfulFlavor()
                    ) { _, url -> Log.d(LOG_TAG, url) }

                    base_linear_layout.removeAllViews()

                    courses.forEach {
                        base_linear_layout.addView(TextView(this@MainActivity).apply {
                            text = "Course: ${it.title}"
                        })
                        it.lessons.forEach {
                            base_linear_layout.addView(TextView(this@MainActivity).apply {
                                text = "Lesson: ${it.title}"
                            })
                            it.modules.forEach {
                                if (it is LessonModule.Copy) {
                                    markyMark.parseMarkdown(it.copy).forEach {
                                        base_linear_layout.addView(it)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (throwable: Throwable) {
                showError(
                    message = throwable.toString(),
                    error = throwable,
                    moreTitle = "Copy to clipboard",
                    moreHandler = {
                        saveToClipboard(
                            "label",
                            "${throwable.message} ${throwable.stackTraceText}"
                        )
                        toast("Copied to clipboard")
                    }
                )
            }
        }
    }
}
