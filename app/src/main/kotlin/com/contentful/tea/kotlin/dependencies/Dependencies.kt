package com.contentful.tea.kotlin.dependencies

import android.content.Context
import com.contentful.tea.kotlin.content.ContentInfrastructure
import com.contentful.tea.kotlin.content.rest.Contentful
import com.contentful.tea.kotlin.markdown.MarkdownProcessor

class Dependencies(
    val contentInfrastructure: ContentInfrastructure,
    val markdown: MarkdownProcessor
) {

    constructor(context: Context) : this(
        Contentful(),
        MarkdownProcessor(context)
    )
}