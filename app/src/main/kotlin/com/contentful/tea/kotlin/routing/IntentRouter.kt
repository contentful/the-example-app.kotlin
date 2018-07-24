package com.contentful.tea.kotlin.routing

const val parameterRegEx = """(\?.+)?"""
const val courseSlugRegEx = """[\w-]+"""
const val categorySlugRegEx = """[\w-]+"""
const val lessonSlugRegEx = """[\w-]+"""

data class Parameter(
    val spaceId: String,
    val previewToken: String,
    val deliveryToken: String,
    val editorialFeatures: Boolean,
    val api: String
) {
    constructor() : this("", "", "", false, "")
}

open class RouteCallback {
    open fun openHome(parameter: Parameter) {}
    open fun goToCourse(courseSlug: String, parameter: Parameter) {}
    open fun goToCategory(categorySlug: String, parameter: Parameter) {}
    open fun goToLesson(courseSlug: String, lessonSlug: String, parameter: Parameter) {}
}

fun route(uri: String, callback: RouteCallback): Boolean {
    val parameter = parameter(uri)

    return when {
        matches(uri, parameterRegEx) -> {
            callback.openHome(parameter)
            true
        }
        matches(uri, "courses/$courseSlugRegEx$parameterRegEx") -> {
            callback.goToCourse(
                courseId(uri),
                parameter
            )
            true
        }
        matches(
            uri,
            "courses/categories/$categorySlugRegEx$parameterRegEx"
        ) -> {
            callback.goToCategory(
                categoryId(uri),
                parameter
            )
            true
        }
        matches(
            uri,
            "courses/$courseSlugRegEx/lessons/$lessonSlugRegEx$parameterRegEx"
        ) -> {
            callback.goToLesson(
                courseId(uri),
                lessonId(uri),
                parameter
            )
            true
        }
        else -> false
    }
}

fun courseId(uri: String): String {
    val (_, courseId) = uri.split("/")
    return courseId
}

fun categoryId(uri: String): String {
    val (_, _, categoryId) = uri.split("/")
    return categoryId
}

fun lessonId(uri: String): String {
    val (_, _, _, lessonId) = uri.split("/")
    return lessonId
}

fun parameter(uri: String): Parameter {
    if (!uri.contains("?")) {
        return Parameter()
    }

    val (_, parameter) = uri.split("?")

    val parameterMap = parameter
        .split("&")
        .map { it.split("=") }
        .filter { it.size == 2 }
        .map { Pair(it[0], it[1]) }
        .toMap()

    return Parameter(
        parameterMap["space_id"].orEmpty(),
        parameterMap["preview_token"].orEmpty(),
        parameterMap["delivery_token"].orEmpty(),
        parameterMap["editorial_features"].enabledOrFalse(),
        parameterMap["api"].orEmpty()
    )
}

fun Any?.enabledOrFalse(): Boolean =
    if (this == null || this !is String) false else this.toLowerCase() == "enabled"

fun matches(uri: String, template: String): Boolean {
    return uri.matches(template.toRegex())
}
