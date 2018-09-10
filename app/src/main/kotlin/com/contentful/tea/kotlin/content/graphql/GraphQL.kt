package com.contentful.tea.kotlin.content.graphql

import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.contentful.tea.kotlin.BuildConfig
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
import okhttp3.OkHttpClient
import java.util.concurrent.CountDownLatch

private const val DEFAULT_LOCALE = "en-US"

open class GraphQL(override var parameter: Parameter = parameterFromBuildConfig()) :
    ContentInfrastructure {

    var apollo: ApolloClient = reset()

    fun reset(
        token: String = BuildConfig.CONTENTFUL_DELIVERY_TOKEN,
        space: String = BuildConfig.CONTENTFUL_SPACE_ID,
        locale: String = "en-US"
    ): ApolloClient {
        val httpClient: OkHttpClient = OkHttpClient
            .Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder().addHeader(
                        "Authorization",
                        "Bearer $token"
                    ).build()
                )
            }
            .build()

        return ApolloClient
            .builder()
            .serverUrl("https://graphql.contentful.com/content/v1/spaces/$space?locale=$locale")
            .callFactory { request ->
                httpClient.newCall(request)
            }
            .build()
    }

    override fun fetchHomeLayout(
        errorCallback: (Throwable) -> Unit,
        successCallback: (Layout) -> Unit
    ) {
        launch {
            try {
                apollo.query(HomeQuery.builder().build())
                    .enqueue(object : ApolloCall.Callback<HomeQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            errorCallback(e)
                        }

                        override fun onResponse(response: Response<HomeQuery.Data>) {
                            successCallback(
                                if (response.data() != null) {
                                    Layout.fromGraphQlEntry(response.data()!!)
                                } else {
                                    Layout("", "", emptyList())
                                }
                            )
                        }
                    })
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
                apollo.query(CourseBySlugQuery.builder().slug(coursesSlug).build())
                    .enqueue(object : ApolloCall.Callback<CourseBySlugQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            errorCallback(e)
                        }

                        override fun onResponse(response: Response<CourseBySlugQuery.Data>) {
                            successCallback(
                                if (response.data() != null) {
                                    Course
                                        .fromGraphQlEntry(
                                            response
                                                .data()
                                                ?.courseCollection
                                                ?.items
                                                .orEmpty()
                                                .first()
                                                .fragments()
                                                .course()
                                        )
                                } else {
                                    Course("", "", "", "", "", 0, "", emptyList(), emptyList())
                                }
                            )
                        }
                    })
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
                apollo.query(CoursesOfCategoryIdQuery.builder().categoryId(categoryId).build())
                    .enqueue(object : ApolloCall.Callback<CoursesOfCategoryIdQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            errorCallback(e)
                        }

                        override fun onResponse(response: Response<CoursesOfCategoryIdQuery.Data>) {
                            successCallback(
                                if (response.data() != null) {
                                    response.data()?.category()
                                        ?.linkedFrom?.entryCollection?.items.orEmpty().map {
                                        Course.fromGraphQlEntry(it.fragments().course()!!)
                                    }
                                } else {
                                    emptyList()
                                }
                            )
                        }
                    })
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
                apollo.query(CoursesQuery.builder().build())
                    .enqueue(object : ApolloCall.Callback<CoursesQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            errorCallback(e)
                        }

                        override fun onResponse(response: Response<CoursesQuery.Data>) {
                            successCallback(
                                if (response.data() != null) {
                                    response.data()?.courseCollection()?.items.orEmpty().map {
                                        Course.fromGraphQlEntry(it.fragments().course())
                                    }
                                } else {
                                    emptyList()
                                }
                            )
                        }
                    })
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
                apollo.query(CategoriesQuery.builder().build())
                    .enqueue(object : ApolloCall.Callback<CategoriesQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            errorCallback(e)
                        }

                        override fun onResponse(response: Response<CategoriesQuery.Data>) {
                            successCallback(
                                if (response.data() != null) {
                                    response.data()?.categoryCollection?.items.orEmpty().map {
                                        Category.fromGraphQlEntry(it.fragments().category())
                                    }
                                } else {
                                    emptyList()
                                }
                            )
                        }
                    })
            } catch (throwable: Throwable) {
                errorCallback(throwable)
            }
        }
    }

    override fun fetchSpace(
        errorCallback: (Throwable) -> Unit,
        successCallback: (Space) -> Unit
    ) {
        launch {
            fetchHomeLayout(errorCallback) {
                successCallback(Space("space_id", "No Space in GraphQL"))
            }
        }
    }

    override fun fetchAllLocales(
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Locale>) -> Unit
    ) {
        successCallback(
            listOf(
                Locale("one", "en-US", "(hardcoded) English"),
                Locale("two", "de-DE", "(fest eingebaut) Deutsch")
            )
        )
    }

    override fun applyParameter(
        parameter: Parameter,
        errorHandler: (Throwable) -> Unit,
        successHandler: (Space) -> Unit
    ) {
        launch {
            try {
                val token = parameter.deliveryToken
                    .or(
                        this@GraphQL.parameter.deliveryToken
                            .or(BuildConfig.CONTENTFUL_DELIVERY_TOKEN)
                    )
                val space = parameter.spaceId.or(this@GraphQL.parameter.spaceId)
                val locale = parameter.locale.or(
                    this@GraphQL.parameter.locale
                        .or(DEFAULT_LOCALE)
                )

                val newApollo = reset(
                    token,
                    space,
                    locale
                )

                if (canFetchHome(newApollo)) {
                    Log.d("GraphQL.kt", "Connected to space.")

                    val currentParameter = this@GraphQL.parameter
                    this@GraphQL.parameter = Parameter(
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

                    apollo = newApollo

                    successHandler(Space(parameter.spaceId, ""))
                } else {
                    val errorMessage = createErrorMessage(parameter.spaceId)
                    Log.e("Contentful.kt", errorMessage)
                    errorHandler(IllegalStateException(errorMessage))
                }
            } catch (throwable: Throwable) {
                Log.e(
                    "Contentful.kt", createErrorMessage(parameter.spaceId),
                    throwable
                )
                errorHandler(throwable)
            }
        }
    }

    private fun createErrorMessage(spaceId: String) = "Cannot connect to space.'$spaceId'."

    private fun canFetchHome(newApollo: ApolloClient): Boolean {
        val latch = CountDownLatch(1)
        var successful = false

        newApollo.query(HomeQuery.builder().build())
            .enqueue(object : ApolloCall.Callback<HomeQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    successful = false
                    latch.countDown()
                }

                override fun onResponse(response: Response<HomeQuery.Data>) {
                    successful = true
                    latch.countDown()
                }
            })

        latch.await()
        return successful
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
