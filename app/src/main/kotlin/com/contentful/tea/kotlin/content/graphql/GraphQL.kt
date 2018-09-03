package com.contentful.tea.kotlin.content.graphql

import android.util.Log
import com.contentful.tea.kotlin.content.Api
import com.contentful.tea.kotlin.content.Category
import com.contentful.tea.kotlin.content.ContentInfrastructure
import com.contentful.tea.kotlin.content.Course
import com.contentful.tea.kotlin.content.Layout
import com.contentful.tea.kotlin.content.Locale
import com.contentful.tea.kotlin.content.Parameter
import com.contentful.tea.kotlin.content.Space
import com.contentful.tea.kotlin.content.parameterFromBuildConfig
import com.contentful.tea.kotlin.extensions.or
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.CountDownLatch

private const val DEFAULT_LOCALE = "en-US"

open class GraphQL(
    override var parameter: Parameter = parameterFromBuildConfig()
) : ContentInfrastructure {
    override fun fetchHomeLayout(
        errorCallback: (Throwable) -> Unit,
        successCallback: (Layout) -> Unit
    ) {
        launch {
        }
    }

    override fun fetchCourseBySlug(
        coursesSlug: String,
        errorCallback: (Throwable) -> Unit,
        successCallback: (Course) -> Unit
    ) {
        launch {
        }
    }

    override fun fetchAllCoursesOfCategoryId(
        categoryId: String,
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Course>) -> Unit
    ) {
        launch {
        }
    }

    override fun fetchAllCourses(
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Course>) -> Unit
    ) {
        launch {
        }
    }

    override fun fetchAllCategories(
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Category>) -> Unit
    ) {
        launch {
        }
    }

    override fun fetchSpace(
        errorCallback: (Throwable) -> Unit,
        successCallback: (Space) -> Unit
    ) {
        launch {
        }
    }

    override fun fetchAllLocales(
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Locale>) -> Unit
    ) {
        launch {
        }
    }

    override fun applyParameter(
        parameter: Parameter,
        errorHandler: (Throwable) -> Unit,
        successHandler: (Space) -> Unit
    ) {
        launch {
            try {
                val ql = createClient(parameter)
                val space = ql.fetchSpace()
                Log.d("GraphQL.kt", """Connected to space "${space.name}".""")

                val currentParameter = this@GraphQL.parameter
                this@GraphQL.parameter = Parameter(
                    spaceId = parameter.spaceId.or(currentParameter.spaceId),
                    previewToken = parameter.previewToken.or(currentParameter.previewToken),
                    locale = parameter.locale.or(currentParameter.locale.or(DEFAULT_LOCALE)),
                    host = parameter.host.or(currentParameter.host),
                    editorialFeature = parameter.editorialFeature,
                    api = parameter.api ?: Api.CDA
                )

                // look if current configured locale is available in space
                parameter.locale = lookUpSuitableLocale()

                successHandler(space)
            } catch (throwable: Throwable) {
                Log.e("Contentful.kt", "Cannot connect to space.", throwable)
                errorHandler(throwable)
            }
        }
    }

    private fun createClient(parameter: Parameter) = object {
        fun fetchSpace() = Space("", "")
    }

    internal open fun lookUpSuitableLocale(): String {
        val latch = CountDownLatch(1)
        var localeCode = "en-US"

        fetchAllLocales({
            latch.countDown()
        }, { locales ->
            localeCode = locales.first { it.code == parameter.locale }.code
            latch.countDown()
        })

        latch.await()
        return localeCode
    }
}
