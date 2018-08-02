package com.contentful.tea.kotlin.contentful

import android.util.Log
import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDAEntry
import com.contentful.java.cda.CDASpace
import com.contentful.tea.kotlin.BuildConfig
import com.contentful.tea.kotlin.contentful.Api.CDA
import com.contentful.tea.kotlin.contentful.Api.CPA
import com.contentful.tea.kotlin.contentful.EditorialFeature.Disabled
import com.contentful.tea.kotlin.contentful.EditorialFeature.Enabled
import kotlinx.coroutines.experimental.launch

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
    val spaceId: String = "",
    val previewToken: String = "",
    val deliveryToken: String = "",
    val editorialFeature: EditorialFeature = Disabled,
    val api: Api = CDA
)

open class Contentful(
    var clientDelivery: CDAClient = CDAClient.builder()
        .setToken(BuildConfig.CONTENTFUL_DELIVERY_TOKEN)
        .setSpace(BuildConfig.CONTENTFUL_SPACE_ID)
        .build(),
    var clientPreview: CDAClient = CDAClient.builder()
        .setToken(BuildConfig.CONTENTFUL_PREVIEW_TOKEN)
        .setSpace(BuildConfig.CONTENTFUL_SPACE_ID)
        .build(),
    var client: CDAClient = clientDelivery,
    var parameter: Parameter = Parameter(
        spaceId = BuildConfig.CONTENTFUL_SPACE_ID,
        deliveryToken = BuildConfig.CONTENTFUL_DELIVERY_TOKEN,
        previewToken = BuildConfig.CONTENTFUL_PREVIEW_TOKEN,
        editorialFeature = EditorialFeature.Disabled,
        api = Api.CDA
    ),
    private val locale: String = "en-US"
) {
    fun fetchHomeLayout(
        errorCallback: (Throwable) -> Unit,
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
                        .include(10)
                        .where("fields.slug", coursesSlug)
                        .all()
                        .items()
                        .first() as CDAEntry,
                    locale
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
                        .linksToEntryId(categoryId)
                        .include(10)
                        .all()
                        .items()
                        .map { Course(it as CDAEntry, locale) }

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
                        .include(10)
                        .all()
                        .items()
                        .map { Course(it as CDAEntry, locale) }

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
                    .include(10)
                    .all()
                    .items()
                    .map { Category(it as CDAEntry, locale) }

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
                    editorialFeature = parameter.editorialFeature,
                    api = parameter.api
                )

                successHandler(deliverySpace)
            } catch (throwable: Throwable) {
                Log.e("Contentful.kt", "Cannot connect to space.")
                errorHandler(throwable)
            }
        }
    }

    internal open fun createClients(parameter: Parameter): Pair<CDAClient, CDAClient> =
        Pair(
            CDAClient.builder().apply {
                setSpace(parameter.spaceId.or(this@Contentful.parameter.spaceId))
                setToken(parameter.deliveryToken.or(this@Contentful.parameter.deliveryToken))
            }.build(),
            CDAClient.builder().apply {
                setSpace(parameter.spaceId.or(this@Contentful.parameter.spaceId))
                setToken(parameter.previewToken.or(this@Contentful.parameter.previewToken))
                preview()
            }.build()
        )
}

fun String?.or(other: String): String = if (isNullOrEmpty()) other else this!!