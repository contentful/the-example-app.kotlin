package com.contentful.tea.kotlin.dependencies

import android.content.Context
import com.contentful.tea.kotlin.content.ContentInfrastructure
import com.contentful.tea.kotlin.content.graphql.GraphQL
import com.contentful.tea.kotlin.markdown.MarkdownProcessor

class Dependencies(
    var contentInfrastructure: ContentInfrastructure,
    val markdown: MarkdownProcessor
) {

    constructor(context: Context) : this(
        GraphQL(),
        MarkdownProcessor(context)
    )
}