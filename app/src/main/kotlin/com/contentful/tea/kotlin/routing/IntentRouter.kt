package com.contentful.tea.kotlin.routing

import com.contentful.tea.kotlin.contentful.Parameter
import com.contentful.tea.kotlin.contentful.toApi
import com.contentful.tea.kotlin.contentful.toEditorialFeature

const val courseSlugRegEx = """[\w-]+"""
const val categorySlugRegEx = """[\w-]+"""
const val lessonSlugRegEx = """[\w-]+"""

open class RouteCallback {
    open fun openHome(parameter: Parameter) {}
    open fun goToCourse(courseSlug: String, parameter: Parameter) {}
    open fun goToCategory(categorySlug: String, parameter: Parameter) {}
    open fun goToLesson(courseSlug: String, lessonSlug: String, parameter: Parameter) {}
}

fun route(path: String, parameter: Parameter, callback: RouteCallback): Boolean {

    return when {
        path.isEmpty() -> {
            callback.openHome(parameter)
            true
        }
        matches(path, "courses/$courseSlugRegEx") -> {
            callback.goToCourse(
                courseId(path),
                parameter
            )
            true
        }
        matches(
            path,
            "courses/categories/$categorySlugRegEx"
        ) -> {
            callback.goToCategory(
                categoryId(path),
                parameter
            )
            true
        }
        matches(
            path,
            "courses/$courseSlugRegEx/lessons/$lessonSlugRegEx"
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
        return Pair(Parameter(), uri)
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
            parameterMap["editorial_features"].toEditorialFeature(),
            parameterMap["api"].toApi()
        ), uri.substringBefore("?")
    )
}

fun matches(uri: String, template: String): Boolean {
    return uri.matches(template.toRegex())
}
