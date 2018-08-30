package com.contentful.tea.kotlin.information

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.contentful.tea.kotlin.BuildConfig
import com.contentful.tea.kotlin.R
import kotlinx.android.synthetic.main.fragment_about.view.*
import kotlinx.android.synthetic.main.item_about_others.view.*

data class Platform(
    val name: String,
    val hosted: String,
    val gitHub: String,
    @DrawableRes val logo: Int
)

class AboutFragment : Fragment() {
    private val platforms: List<Platform> = listOf(
        Platform(
            "Java",
            "https://the-example-app-java.contentful.com",
            "https://github.com/contentful/the-example-app.java",
            R.drawable.icon_java
        ),
        Platform(
            "JavaScript",
            "https://the-example-app-nodejs.contentful.com/",
            "https://github.com/contentful/the-example-app.nodejs",
            R.drawable.icon_nodejs
        ),
        Platform(
            ".Net",
            "https://the-example-app-csharp.contentful.com",
            "https://github.com/contentful/the-example-app.csharp",
            R.drawable.icon_dotnet
        ),
        Platform(
            "Ruby",
            "https://the-example-app-rb.contentful.com",
            "https://github.com/contentful/the-example-app.rb",
            R.drawable.icon_ruby
        ),
        Platform(
            "Php",
            "https://the-example-app-php.contentful.com",
            "https://github.com/contentful/the-example-app.php",
            R.drawable.icon_php
        ),
        Platform(
            "Python",
            "https://the-example-app-py.contentful.com",
            "https://github.com/contentful/the-example-app.py",
            R.drawable.icon_python
        ),
        Platform(
            "Swift",
            "",
            "https://github.com/contentful/the-example-app.swift",
            R.drawable.icon_swift
        ),
        Platform(
            "Android",
            "",
            "https://github.com/contentful/the-example-app.kotlin",
            R.drawable.icon_android
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_about, container, false)

        root.about_description.apply {
            text = Html.fromHtml(getString(R.string.about_description), 0)
            movementMethod = LinkMovementMethod.getInstance()
        }

        platforms.map { platform ->
            inflater.inflate(R.layout.item_about_others, root.about_others, false).apply {
                this.about_others_logo.setBackgroundResource(platform.logo)
                this.about_others_logo.setOnClickListener {
                    if (platform.hosted.isEmpty()) {
                        openLink(platform.gitHub)
                    } else {
                        openLink(platform.hosted)
                    }
                }

                root.about_others.addView(this)
            }
        }

        @SuppressWarnings("SetTextI18n")
        root.about_version.text = "v${BuildConfig.VERSION_NAME}"

        return root
    }

    private fun openLink(uri: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
    }
}
