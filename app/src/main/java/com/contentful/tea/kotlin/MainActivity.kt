package com.contentful.tea.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDAEntry
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
            val entries = client.fetch(CDAEntry::class.java).all()
            val titles = entries.entries().values.joinToString("\n") { it.localize("en-US").getField("title") as String }
            Log.d(LOG_TAG, "Found ${entries.total()} entries.")
            runOnUiThread {
                test_text.text = Html.fromHtml("<b>${titles.replace("\n", "</b><br/><b>")}</b>", Html.FROM_HTML_MODE_LEGACY)
            }
        }

    }
}
