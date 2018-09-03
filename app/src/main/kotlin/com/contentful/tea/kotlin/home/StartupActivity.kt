package com.contentful.tea.kotlin.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.contentful.tea.kotlin.MainActivity
import com.contentful.tea.kotlin.R
import com.contentful.tea.kotlin.content.rest.Contentful
import com.contentful.tea.kotlin.extensions.isNetworkError
import com.contentful.tea.kotlin.extensions.showError
import com.contentful.tea.kotlin.extensions.showNetworkError
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
            Contentful().fetchHomeLayout(::error) {
                val intent = Intent(this@StartupActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }
    }

    private fun error(throwable: Throwable) {
        if (throwable.isNetworkError()) {
            showNetworkError()
        } else {
            showError(
                message = getString(R.string.error_fetching_layout),
                error = throwable
            )
        }
    }
}
