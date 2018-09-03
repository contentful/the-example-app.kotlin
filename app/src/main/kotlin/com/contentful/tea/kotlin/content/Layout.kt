package com.contentful.tea.kotlin.content

data class Layout(
    val title: String,
    val slug: String,
    val contentModules: List<LayoutModule>
) {
    companion object
}

sealed class LayoutModule {
    data class Copy(
        val title: String,
        val headline: String,
        val copy: String,
        val ctaTitle: String,
        val ctaLink: String,
        val visualStyle: String
    ) : LayoutModule() {
        companion object
    }

    data class HeroImage(
        val title: String,
        val headline: String,
        val backgroundImage: String

    ) : LayoutModule() {
        companion object
    }

    data class HightlightedCourse(
        val title: String,
        val course: Course
    ) : LayoutModule() {
        companion object
    }
}