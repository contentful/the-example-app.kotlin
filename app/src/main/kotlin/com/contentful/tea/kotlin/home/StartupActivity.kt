package com.contentful.tea.kotlin.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.contentful.java.cda.CDAEntry
import com.contentful.tea.kotlin.MainActivity
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.contentful.Contentful
import com.contentful.tea.kotlin.extensions.showError
import kotlinx.coroutines.experimental.launch

class StartupActivity : AppCompatActivity() {

    private var alreadyStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
    }

    override fun onResume() {
        if (!alreadyStarted) {
            alreadyStarted = true
            requestContentful()
        }
        super.onResume()
    }

    private fun requestContentful() {
        launch {
            val client = Contentful().client
            try {
                // populate cache
                client
                    .fetch(CDAEntry::class.java)
                    .withContentType("course")
                    .include(10)
                    .all()

                val intent = Intent(this@StartupActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } catch (throwable: Throwable) {
                showError(
                    "Error while fetching content from Contentful.",
                    error = throwable
                )
            }
        }
    }
}
