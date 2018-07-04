package com.contentful.tea.kotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.contentful.java.cda.CDAEntry
import com.contentful.tea.kotlin.contentful.Contentful
import com.contentful.tea.kotlin.contentful.Course
import com.contentful.tea.kotlin.extensions.showError
import com.contentful.tea.kotlin.extensions.showInformation
import kotlinx.coroutines.experimental.launch

/**
 * This fragment is used to display a startup screen while the connection to contentful is
 * established.
 */
class StartupFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestContentful()
    }

    private fun requestContentful() {
        val client = Contentful().client

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


                activity?.apply {
                    showInformation("Found ${courses.size} entries.")
                    runOnUiThread {
                        NavHostFragment.findNavController(this@StartupFragment)
                            .navigate(
                                StartupFragmentDirections.startupDone()
                            )
                    }
                }
            } catch (throwable: Throwable) {
                activity?.showError(
                    "Error while fetching content from Contentful.",
                    error = throwable
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_startup, container, false)
    }
}

const val LOG_TAG: String = "com.contentful.tea.kotlin.Startup"