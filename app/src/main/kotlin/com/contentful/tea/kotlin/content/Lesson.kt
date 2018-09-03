package com.contentful.tea.kotlin.content

data class Lesson(
    val title: String,
    val slug: String,
    val modules: List<LessonModule>
) {
    companion object
}

sealed class LessonModule {
    data class CodeSnippet(
        val title: String,
        val curl: String,
        val dotNet: String,
        val javascript: String,
        val java: String,
        val javaAndroid: String,
        val php: String,
        val python: String,
        val ruby: String,
        val swift: String
    ) : LessonModule() {
        companion object
    }

    data class Image(
        val title: String,
        val caption: String,
        val image: String
    ) : LessonModule() {
        companion object
    }

    data class Copy(
        val title: String,
        val copy: String
    ) : LessonModule() {
        companion object
    }
}
