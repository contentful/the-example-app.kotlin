package com.contentful.tea.kotlin.contentful

import com.contentful.java.cda.CDAAsset
import com.contentful.java.cda.CDAEntry
import com.contentful.java.cda.image.ImageOption.Format.webp
import com.contentful.java.cda.image.ImageOption.formatOf
import com.contentful.java.cda.image.ImageOption.https

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
    constructor(entry: CDAEntry, locale: String) : this(
        entry.getField<String?>(locale, "title").orEmpty(),
        entry.getField<String?>(locale, "slug").orEmpty(),
        try {
            entry.getField<CDAAsset?>(locale, "image")
                ?.urlForImageWith(https(), formatOf(webp)).orEmpty()
        } catch (_: Throwable) {
            ""
        },
        entry.getField<String?>(locale, "shortDescription").orEmpty(),
        entry.getField<String?>(locale, "description").orEmpty(),
        entry.getField<Double?>(locale, "duration").or(0.0).toInt(),
        entry.getField<String?>(locale, "skillLevel").orEmpty(),
        entry.getField<List<CDAEntry>?>(locale, "lessons")
            .orEmpty()
            .map { Lesson(it, locale) },
        entry.getField<List<CDAEntry>>(locale, "categories")
            .orEmpty()
            .map { Category(it, locale) }
    )
}

private fun Double?.or(default: Double): Double = this ?: default

data class Category(
    val id: String,
    val title: String,
    val slug: String
) {
    constructor(entry: CDAEntry, locale: String) : this(
        entry.id(),
        entry.getField<String?>(locale, "title").orEmpty(),
        entry.getField<String?>(locale, "slug").orEmpty()
    )
}
