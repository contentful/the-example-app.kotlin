package com.contentful.tea.kotlin.contentful

import android.util.Log
import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDAEntry
import com.contentful.tea.kotlin.BuildConfig
import com.contentful.tea.kotlin.routing.Parameter
import kotlinx.coroutines.experimental.launch

class Contentful(
    val client: CDAClient = CDAClient.builder()
        .setToken(BuildConfig.CONTENTFUL_DELIVERY_TOKEN)
        .setSpace(BuildConfig.CONTENTFUL_SPACE_ID)
        .build(),
    private val locale: String = "en-US"
) {
    fun fetchHomeLayout(
        errorCallback: (Throwable) -> Unit = ::defaultError,
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
        errorCallback: (Throwable) -> Unit = ::defaultError,
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
        errorCallback: (Throwable) -> Unit = ::defaultError,
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
        errorCallback: (Throwable) -> Unit = ::defaultError,
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
        errorCallback: (Throwable) -> Unit = ::defaultError,
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

    private fun defaultError(t: Throwable) {
        Log.e(TAG, "Failure in fetching from Contentful", t)
    }

    fun applyParameter(parameter: Parameter) {
        // TODO Use parameters to request "stuff" from contentful
    }

    companion object {
        private val TAG: String = Contentful::class.simpleName!!
    }
}