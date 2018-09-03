package com.contentful.tea.kotlin.content

data class Course(
    val title: String,
    val slug: String,
    val image: String,
    val shortDescription: String,
    val description: String,
    val duration: Int,
    val skillLevel: String,
    val lessons: List<Lesson>,
    val categories: List<Category>
) {
    companion object
}

data class Category(
    val id: String,
    val title: String,
    val slug: String
) {
    companion object
}
