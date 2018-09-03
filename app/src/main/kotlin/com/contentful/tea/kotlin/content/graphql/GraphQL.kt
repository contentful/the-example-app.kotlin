package com.contentful.tea.kotlin.content.graphql

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.contentful.tea.kotlin.BuildConfig
import com.contentful.tea.kotlin.content.Category
import com.contentful.tea.kotlin.content.ContentInfrastructure
import com.contentful.tea.kotlin.content.Course
import com.contentful.tea.kotlin.content.Layout
import com.contentful.tea.kotlin.content.Locale
import com.contentful.tea.kotlin.content.Space
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient

open class GraphQL() : ContentInfrastructure {

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
                                                .courseFragment()
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
                                        Course.fromGraphQlEntry(it.fragments().courseFragment()!!)
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
                                        Course.fromGraphQlEntry(it.fragments().courseFragment())
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
                                        Category.fromGraphQlEntry(it.fragments().categoryFragment())
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
}
