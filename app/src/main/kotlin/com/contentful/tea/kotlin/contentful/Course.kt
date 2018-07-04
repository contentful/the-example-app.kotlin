package com.contentful.tea.kotlin.data

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
            entry.getField<String>(locale, "title"),
            entry.getField<String>(locale, "slug"),
            entry.getField<CDAAsset>(locale, "image")
                    .urlForImageWith(https(), formatOf(webp)),
            entry.getField<String>(locale, "shortDescription"),
            entry.getField<String>(locale, "description"),
            entry.getField<Double>(locale, "duration").toInt(),
            entry.getField<String>(locale, "skillLevel"),
            entry.getField<List<CDAEntry>>(locale, "lessons")
                    .map { Lesson(it, locale) },
            entry.getField<List<CDAEntry>>(locale, "categories")
                    .map { Category(it, locale) }
    )
}

data class Category(
    val title: String,
    val slug: String
) {
    constructor(entry: CDAEntry, locale: String) : this(
            entry.getField<String>(locale, "title"),
            entry.getField<String>(locale, "slug")
    )
}
