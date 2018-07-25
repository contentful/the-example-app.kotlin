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

fun route(pathAndParameter: String, callback: RouteCallback): Boolean {
    val (parameter, path) = separateParameterFromPath(pathAndParameter)

    return when {
        matches(path, parameterRegEx) -> {
            callback.openHome(parameter)
            true
        }
        matches(path, "courses/$courseSlugRegEx$parameterRegEx") -> {
            callback.goToCourse(
                courseId(path),
                parameter
            )
            true
        }
        matches(
            path,
            "courses/categories/$categorySlugRegEx$parameterRegEx"
        ) -> {
            callback.goToCategory(
                categoryId(path),
                parameter
            )
            true
        }
        matches(
            path,
            "courses/$courseSlugRegEx/lessons/$lessonSlugRegEx$parameterRegEx"
        ) -> {
            callback.goToLesson(
                courseId(path),
                lessonId(path),
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

fun separateParameterFromPath(uri: String): Pair<Parameter, String> {
    if (!uri.contains("?")) {
        return Pair(Parameter(), "")
    }

    val (_, parameter) = uri.split("?")

    val parameterMap = parameter
        .split("&")
        .map { it.split("=") }
        .filter { it.size == 2 }
        .map { Pair(it[0], it[1]) }
        .toMap()

    return Pair(
        Parameter(
            parameterMap["space_id"].orEmpty(),
            parameterMap["preview_token"].orEmpty(),
            parameterMap["delivery_token"].orEmpty(),
            parameterMap["editorial_features"].enabledOrFalse(),
            parameterMap["api"].orEmpty()
        ), uri.substringBefore("?")
    )
}

fun Any?.enabledOrFalse(): Boolean =
    if (this == null || this !is String) false else this.toLowerCase() == "enabled"

fun matches(uri: String, template: String): Boolean {
    return uri.matches(template.toRegex())
}
