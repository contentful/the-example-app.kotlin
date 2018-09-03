package com.contentful.tea.kotlin.content.rest

import android.util.Log
import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDAEntry
import com.contentful.java.cda.CDALocale
import com.contentful.java.cda.CDASpace
import com.contentful.tea.kotlin.BuildConfig
import com.contentful.tea.kotlin.content.Api
import com.contentful.tea.kotlin.content.Category
import com.contentful.tea.kotlin.content.ContentInfrastructure
import com.contentful.tea.kotlin.content.Course
import com.contentful.tea.kotlin.content.Layout
import com.contentful.tea.kotlin.content.Parameter
import com.contentful.tea.kotlin.content.parameterFromBuildConfig
import kotlinx.coroutines.experimental.launch
import java.util.NoSuchElementException

private const val DEFAULT_LOCALE = "en-US"

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
    override var parameter: Parameter = parameterFromBuildConfig()
) : ContentInfrastructure {
    override fun fetchHomeLayout(
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
                    .map {
                        Layout.fromRestEntry(
                            it as CDAEntry,
                            parameter.locale.or(DEFAULT_LOCALE)
                        )
                    }
                    .first { it.contentModules.isNotEmpty() }

                successCallback(layout)
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    override fun fetchCourseBySlug(
        coursesSlug: String,
        errorCallback: (Throwable) -> Unit,
        successCallback: (Course) -> Unit
    ) {
        launch {
            try {
                val course = Course.fromRestEntry(
                    client
                        .fetch(CDAEntry::class.java)
                        .withContentType("course")
                        .where(
                            "locale",
                            parameter.locale.or(DEFAULT_LOCALE)
                        )
                        .include(10)
                        .where("fields.slug", coursesSlug)
                        .all()
                        .items()
                        .first() as CDAEntry,
                    parameter.locale ?: DEFAULT_LOCALE
                )

                successCallback(course)
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    override fun fetchAllCoursesOfCategoryId(
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
                        .where("locale", parameter.locale ?: DEFAULT_LOCALE)
                        .linksToEntryId(categoryId)
                        .include(10)
                        .all()
                        .items()
                        .map {
                            Course.fromRestEntry(
                                it as CDAEntry,
                                parameter.locale
                                    ?: DEFAULT_LOCALE
                            )
                        }

                successCallback(courses)
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    override fun fetchAllCourses(
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Course>) -> Unit
    ) {
        launch {
            try {
                val courses =
                    client
                        .fetch(CDAEntry::class.java)
                        .withContentType("course")
                        .where("locale", parameter.locale ?: DEFAULT_LOCALE)
                        .include(10)
                        .all()
                        .items()
                        .map {
                            Course.fromRestEntry(
                                it as CDAEntry,
                                parameter.locale
                                    ?: DEFAULT_LOCALE
                            )
                        }

                successCallback(courses)
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    override fun fetchAllCategories(
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Category>) -> Unit
    ) {
        launch {
            try {
                val categories = client
                    .fetch(CDAEntry::class.java)
                    .withContentType("category")
                    .where("locale", parameter.locale ?: DEFAULT_LOCALE)
                    .include(10)
                    .all()
                    .items()
                    .map {
                        Category.fromRestEntry(
                            it as CDAEntry,
                            parameter.locale
                                ?: DEFAULT_LOCALE
                        )
                    }

                successCallback(categories)
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    override fun fetchSpace(
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

    override fun fetchAllLocales(
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

    override fun applyParameter(
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
                client = if (parameter.api == null || parameter.api == Api.CDA) {
                    clientDelivery
                } else {
                    clientPreview
                }

                val currentParameter = this@Contentful.parameter
                this@Contentful.parameter = Parameter(
                    spaceId = parameter.spaceId.or(currentParameter.spaceId),
                    deliveryToken = parameter.deliveryToken.or(currentParameter.deliveryToken),
                    previewToken = parameter.previewToken.or(currentParameter.previewToken),
                    locale = parameter.locale.or(currentParameter.locale.or(DEFAULT_LOCALE)),
                    host = parameter.host.or(currentParameter.host),
                    editorialFeature = parameter.editorialFeature,
                    api = parameter.api ?: Api.CDA
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
            parameter.host.or(BuildConfig.CONTENTFUL_HOST)
        else
            this@Contentful.parameter.host

        this@Contentful.parameter.host = host

        val subdomain = if (preview) "preview" else "cdn"
        return "https://$subdomain.$host/"
    }

    private fun areSomeSpaceParameterSet(parameter: Parameter): Boolean =
        parameter.spaceId.isNotEmpty() ||
            parameter.deliveryToken.isNotEmpty() ||
            parameter.previewToken.isNotEmpty()
}

fun String?.or(other: String): String = if (isNullOrEmpty()) other else this!!