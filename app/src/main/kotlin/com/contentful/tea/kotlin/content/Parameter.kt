package com.contentful.tea.kotlin.content

import com.contentful.tea.kotlin.BuildConfig

enum class Api {
    CDA,
    CPA
}

fun String?.toApi(): Api = if (this == null || this.toLowerCase() == "cda") Api.CDA else Api.CPA
enum class EditorialFeature {
    Enabled,
    Disabled
}

fun String?.toEditorialFeature(): EditorialFeature =
    if (this == null || this.toLowerCase() == "enabled") {
        EditorialFeature.Enabled
    } else {
        EditorialFeature.Disabled
    }

data class Parameter(
    var spaceId: String = "",
    var previewToken: String = "",
    var deliveryToken: String = "",
    var editorialFeature: EditorialFeature = EditorialFeature.Disabled,
    var api: Api? = null,
    var locale: String? = null,
    var host: String = ""
)

fun parameterFromBuildConfig(): Parameter =
    Parameter(
        spaceId = BuildConfig.CONTENTFUL_SPACE_ID,
        deliveryToken = BuildConfig.CONTENTFUL_DELIVERY_TOKEN,
        previewToken = BuildConfig.CONTENTFUL_PREVIEW_TOKEN,
        editorialFeature = EditorialFeature.Disabled,
        host = BuildConfig.CONTENTFUL_HOST,
        api = Api.CDA,
        locale = "en-US"
    )

fun Parameter.toUrl(): String =
    "the-example-app-mobile://" +
        "?space_id=$spaceId" +
        "&preview_token=$previewToken" +
        "&delivery_token=$deliveryToken" +
        "&editorial_features=${editorialFeature.name.toLowerCase()}" +
        "&api=${(if (api == null) Api.CDA.name else api!!.name).toLowerCase()}" +
        "&host=$host"
