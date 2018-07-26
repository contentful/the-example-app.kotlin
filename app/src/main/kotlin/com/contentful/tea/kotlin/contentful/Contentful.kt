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
    val spaceId: String,
    val previewToken: String,
    val deliveryToken: String,
    val editorialFeature: EditorialFeature,
    val api: Api
) {
    constructor() : this("", "", "", Disabled, CDA)

    fun isEmpty(): Boolean = spaceId.isEmpty() && previewToken.isEmpty() && deliveryToken.isEmpty()
}

class Contentful(
    private var client: CDAClient = CDAClient.builder()
        .setToken(BuildConfig.CONTENTFUL_DELIVERY_TOKEN)
        .setSpace(BuildConfig.CONTENTFUL_SPACE_ID)
        .build(),
    private var parameter: Parameter = Parameter(),
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
        if (parameter.isEmpty()) {
            // empty parameter: no need to reevaluate the old one
            launch {
                try {
                    successHandler(client.fetchSpace())
                } catch (throwable: Throwable) {
                    Log.e(TAG, "Cannot connect to predefined space.")
                    errorHandler(throwable)
                }
            }
        } else {
            val clientWithNewConfiguration = CDAClient.builder()
                .setToken(parameter.deliveryToken)
                .setSpace(parameter.spaceId)
                .build()

            launch {
                try {
                    val space = clientWithNewConfiguration.fetchSpace()
                    Log.d(TAG, """Connected to space "${space.name()}".""")
                    this@Contentful.client = clientWithNewConfiguration
                    this@Contentful.parameter = parameter

                    successHandler(space)
                } catch (throwable: Throwable) {
                    Log.e(TAG, "Cannot connect to space.")
                    errorHandler(throwable)
                }
            }
        }
    }

    companion object {
        private val TAG: String = Contentful::class.simpleName!!
    }
}