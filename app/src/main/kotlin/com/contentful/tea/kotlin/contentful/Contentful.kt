package com.contentful.tea.kotlin.contentful

import android.util.Log
import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDAEntry
import com.contentful.java.cda.CDALocale
import com.contentful.java.cda.CDASpace
import com.contentful.tea.kotlin.BuildConfig
import com.contentful.tea.kotlin.contentful.Api.CDA
import com.contentful.tea.kotlin.contentful.Api.CPA
import com.contentful.tea.kotlin.contentful.EditorialFeature.Disabled
import com.contentful.tea.kotlin.contentful.EditorialFeature.Enabled
import kotlinx.coroutines.experimental.launch
import java.util.NoSuchElementException

enum class Api {
    CDA,
    CPA
}

fun String?.toApi(): Api = if (this == null || this.toLowerCase() == "cda") CDA else CPA

enum class EditorialFeature {
    Enabled,
    Disabled
}

fun String?.toEditorialFeature(): EditorialFeature =
    if (this == null || this.toLowerCase() == "enabled") {
        Enabled
    } else {
        Disabled
    }

data class Parameter(
    var spaceId: String = "",
    var previewToken: String = "",
    var deliveryToken: String = "",
    var editorialFeature: EditorialFeature = Disabled,
    var api: Api = CDA,
    var locale: String = "en-US",
    var host: String = ""
)

fun parameterFromBuildConfig(): Parameter = Parameter(
    spaceId = BuildConfig.CONTENTFUL_SPACE_ID,
    deliveryToken = BuildConfig.CONTENTFUL_DELIVERY_TOKEN,
    previewToken = BuildConfig.CONTENTFUL_PREVIEW_TOKEN,
    editorialFeature = EditorialFeature.Disabled,
    host = BuildConfig.CONTENTFUL_HOST,
    api = Api.CDA,
    locale = "en-US"
)

open class Contentful(
    private var clientDelivery: CDAClient = CDAClient.builder()
        .setToken(BuildConfig.CONTENTFUL_DELIVERY_TOKEN)
        .setSpace(BuildConfig.CONTENTFUL_SPACE_ID)
        .build(),
    private var clientPreview: CDAClient = CDAClient.builder()
        .setToken(BuildConfig.CONTENTFUL_PREVIEW_TOKEN)
        .setSpace(BuildConfig.CONTENTFUL_SPACE_ID)
        .build(),
    var client: CDAClient = clientDelivery,
    var parameter: Parameter = parameterFromBuildConfig()
) {
    open fun fetchHomeLayout(
        errorCallback: (Throwable) -> Unit,
        successCallback: (Layout) -> Unit
    ) {
        launch {
            try {
                val layout = client
                    .fetch(CDAEntry::class.java)
                    .withContentType("layout")
                    .where("locale", parameter.locale)
                    .include(10)
                    .all()
                    .items()
                    .map { Layout(it as CDAEntry, parameter.locale) }
                    .first { it.contentModules.isNotEmpty() }

                successCallback(layout)
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    fun fetchCourseBySlug(
        coursesSlug: String,
        errorCallback: (Throwable) -> Unit,
        successCallback: (Course) -> Unit
    ) {
        launch {
            try {
                val course = Course(
                    client
                        .fetch(CDAEntry::class.java)
                        .withContentType("course")
                        .where("locale", parameter.locale)
                        .include(10)
                        .where("fields.slug", coursesSlug)
                        .all()
                        .items()
                        .first() as CDAEntry,
                    parameter.locale
                )

                successCallback(course)
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    fun fetchAllCoursesOfCategoryId(
        categoryId: String,
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Course>) -> Unit
    ) {
        launch {
            try {
                val courses =
                    client
                        .fetch(CDAEntry::class.java)
                        .withContentType("course")
                        .where("locale", parameter.locale)
                        .linksToEntryId(categoryId)
                        .include(10)
                        .all()
                        .items()
                        .map { Course(it as CDAEntry, parameter.locale) }

                successCallback(courses)
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    fun fetchAllCourses(
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Course>) -> Unit
    ) {
        launch {
            try {
                val courses =
                    client
                        .fetch(CDAEntry::class.java)
                        .withContentType("course")
                        .where("locale", parameter.locale)
                        .include(10)
                        .all()
                        .items()
                        .map { Course(it as CDAEntry, parameter.locale) }

                successCallback(courses)
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    fun fetchAllCategories(
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Category>) -> Unit
    ) {
        launch {
            try {
                val categories = client
                    .fetch(CDAEntry::class.java)
                    .withContentType("category")
                    .where("locale", parameter.locale)
                    .include(10)
                    .all()
                    .items()
                    .map { Category(it as CDAEntry, parameter.locale) }

                successCallback(categories)
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    fun fetchSpace(
        errorCallback: (Throwable) -> Unit,
        successCallback: (CDASpace) -> Unit
    ) {
        launch {
            try {
                val space = client.fetchSpace()

                successCallback(space)
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    fun fetchAllLocales(
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<CDALocale>) -> Unit
    ) {
        launch {
            try {
                val categories = client
                    .fetch(CDALocale::class.java)
                    .all()
                    .items()
                    .map { it as CDALocale }

                successCallback(categories)
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    fun applyParameter(
        parameter: Parameter,
        errorHandler: (Throwable) -> Unit,
        successHandler: (CDASpace) -> Unit
    ) {
        launch {
            val (newClientDelivery, newClientPreview) = createClients(parameter)

            try {
                val deliverySpace = newClientDelivery.fetchSpace()
                val previewSpace = newClientPreview.fetchSpace()

                if (deliverySpace.name() != previewSpace.name()) {
                    throw IllegalStateException(
                        "delivery and preview space names cannot be different!"
                    )
                }
                Log.d("Contentful.kt", """Connected to space "${deliverySpace.name()}".""")

                clientDelivery = newClientDelivery
                clientPreview = newClientPreview
                client = if (parameter.api == Api.CDA) {
                    clientDelivery
                } else {
                    clientPreview
                }

                val currentParameter = this@Contentful.parameter
                this@Contentful.parameter = Parameter(
                    spaceId = parameter.spaceId.or(currentParameter.spaceId),
                    deliveryToken = parameter.deliveryToken.or(currentParameter.deliveryToken),
                    previewToken = parameter.previewToken.or(currentParameter.previewToken),
                    locale = parameter.locale.or(currentParameter.locale),
                    host = parameter.host.or(currentParameter.host),
                    editorialFeature = parameter.editorialFeature,
                    api = parameter.api
                )

                // look if current configured locale is available in space
                parameter.locale = lookUpSuitableLocale()

                successHandler(deliverySpace)
            } catch (throwable: Throwable) {
                Log.e("Contentful.kt", "Cannot connect to space.", throwable)
                errorHandler(throwable)
            }
        }
    }

    internal open fun lookUpSuitableLocale(): String {
        val locales = client
            .fetch(CDALocale::class.java)
            .all()
            .items()
            .map { it as CDALocale }

        return try {
            locales.first { it.code() == parameter.locale }.code()
        } catch (_: NoSuchElementException) {
            locales.first { it.isDefaultLocale }.code()
        }
    }

    internal open fun createClients(parameter: Parameter): Pair<CDAClient, CDAClient> =
        Pair(
            CDAClient.builder().apply {
                setSpace(parameter.spaceId.or(this@Contentful.parameter.spaceId))
                setToken(parameter.deliveryToken.or(this@Contentful.parameter.deliveryToken))
                setEndpoint(endpoint(parameter, false))
            }.build(),
            CDAClient.builder().apply {
                setSpace(parameter.spaceId.or(this@Contentful.parameter.spaceId))
                setToken(parameter.previewToken.or(this@Contentful.parameter.previewToken))
                preview()
                setEndpoint(endpoint(parameter, true))
            }.build()
        )

    private fun endpoint(parameter: Parameter, preview: Boolean): String {
        val host = if (areSomeSpaceParameterSet(parameter))
            parameter.host.or(this@Contentful.parameter.host)
        else
            this@Contentful.parameter.host

        val subdomain = if (preview) "preview" else "cdn"
        return "https://$subdomain.$host/"
    }

    private fun areSomeSpaceParameterSet(parameter: Parameter): Boolean =
        parameter.spaceId.isNotEmpty() ||
            parameter.deliveryToken.isNotEmpty() ||
            parameter.previewToken.isNotEmpty()
}

fun String?.or(other: String): String = if (isNullOrEmpty()) other else this!!

fun Parameter.toUrl(): String =
    "the-example-app-mobile://" +
        "?space_id=$spaceId" +
        "&preview_token=$previewToken" +
        "&delivery_token=$deliveryToken" +
        "&editorial_features=${editorialFeature.name.toLowerCase()}" +
        "&api=${api.name.toLowerCase()}" +
        "&host=$host"
