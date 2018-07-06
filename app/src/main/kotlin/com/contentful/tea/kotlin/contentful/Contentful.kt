package com.contentful.tea.kotlin.contentful

import android.util.Log
import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDAEntry
import com.contentful.tea.kotlin.BuildConfig
import kotlinx.coroutines.experimental.launch

class Contentful(
    val client: CDAClient = CDAClient.builder()
        .setToken(BuildConfig.CONTENTFUL_DELIVERY_TOKEN)
        .setSpace(BuildConfig.CONTENTFUL_SPACE_ID)
        .build(),
    private val locale: String = "en-US"
) {
    fun fetchHomeLayout(
        errorCallback: (Throwable) -> Unit = ::defaultError,
        successCallback: (Layout) -> Unit
    ) {
        launch {
            try {
                val layout = client
                    .fetch(CDAEntry::class.java)
                    .withContentType("layout")
                    .include(10)
                    .all()
                    .items()
                    .map { Layout(it as CDAEntry, locale) }
                    .first { it.contentModules.isNotEmpty() }

                successCallback(layout)
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    private fun defaultError(t: Throwable) {
        Log.e(TAG, "Failure in fetching from Contentful", t)
    }

    companion object {
        private val TAG: String = Contentful::class.simpleName!!
    }
}