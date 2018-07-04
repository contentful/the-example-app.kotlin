package com.contentful.tea.kotlin.contentful

import com.contentful.java.cda.CDAClient
import com.contentful.tea.kotlin.BuildConfig

class Contentful(
    val client: CDAClient = CDAClient.builder()
        .setToken(BuildConfig.CONTENTFUL_DELIVERY_TOKEN)
        .setSpace(BuildConfig.CONTENTFUL_SPACE_ID)
        .build()
)