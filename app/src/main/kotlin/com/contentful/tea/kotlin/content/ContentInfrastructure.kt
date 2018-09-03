package com.contentful.tea.kotlin.content

interface ContentInfrastructure {
    fun fetchHomeLayout(
        errorCallback: (Throwable) -> Unit,
        successCallback: (Layout) -> Unit
    )

    fun fetchCourseBySlug(
        coursesSlug: String,
        errorCallback: (Throwable) -> Unit,
        successCallback: (Course) -> Unit
    )

    fun fetchAllCoursesOfCategoryId(
        categoryId: String,
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Course>) -> Unit
    )

    fun fetchAllCourses(
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Course>) -> Unit
    )

    fun fetchAllCategories(
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Category>) -> Unit
    )

    fun fetchSpace(
        errorCallback: (Throwable) -> Unit,
        successCallback: (Space) -> Unit
    )

    fun fetchAllLocales(
        errorCallback: (Throwable) -> Unit,
        successCallback: (List<Locale>) -> Unit
    )
}