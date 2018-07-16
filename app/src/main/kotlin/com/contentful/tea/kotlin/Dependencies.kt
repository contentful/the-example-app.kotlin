package com.contentful.tea.kotlin

import android.content.Context
import com.contentful.tea.kotlin.contentful.Contentful
import com.contentful.tea.kotlin.markdown.MarkdownProcessor

class Dependencies(
    val contentful: Contentful,
    val markdown: MarkdownProcessor
) {

    constructor(context: Context) : this(
        Contentful(),
        MarkdownProcessor(context)
    )
}