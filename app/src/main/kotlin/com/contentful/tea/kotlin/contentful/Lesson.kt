package com.contentful.tea.kotlin.contentful

import com.contentful.java.cda.CDAAsset
import com.contentful.java.cda.CDAEntry
import com.contentful.java.cda.image.ImageOption

data class Lesson(
    val title: String,
    val slug: String,
    val modules: List<LessonModule>
) {
    constructor(entry: CDAEntry, locale: String) : this(
        entry.getField<String?>(locale, "title").orEmpty(),
        entry.getField<String?>(locale, "slug").orEmpty(),
        entry.getField<List<CDAEntry>?>(locale, "modules")
            .orEmpty()
            .map { findLessonModule(it, locale) }
    )
}

fun findLessonModule(entry: CDAEntry, locale: String): LessonModule =
    when (entry.contentType().id()) {
        "lessonCodeSnippets" -> LessonModule.CodeSnippet(entry, locale)
        "lessonImage" -> LessonModule.Image(entry, locale)
        "lessonCopy" -> LessonModule.Copy(entry, locale)
        else -> LessonModule.Copy(
            "<lesson module type not found>",
            "## lesson module type not found"
        )
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
        constructor(entry: CDAEntry, locale: String) : this(
            entry.getField<String?>(locale, "title").orEmpty(),
            entry.getField<String?>(locale, "curl").orEmpty(),
            entry.getField<String?>(locale, "dotNet").orEmpty(),
            entry.getField<String?>(locale, "javascript").orEmpty(),
            entry.getField<String?>(locale, "java").orEmpty(),
            entry.getField<String?>(locale, "javaAndroid").orEmpty(),
            entry.getField<String?>(locale, "php").orEmpty(),
            entry.getField<String?>(locale, "python").orEmpty(),
            entry.getField<String?>(locale, "ruby").orEmpty(),
            entry.getField<String?>(locale, "swift").orEmpty()
        )
    }

    data class Image(
        val title: String,
        val caption: String,
        val image: String
    ) : LessonModule() {
        constructor(entry: CDAEntry, locale: String) : this(
            entry.getField<String?>(locale, "title").orEmpty(),
            entry.getField<String?>(locale, "caption").orEmpty(),
            try {
                entry.getField<CDAAsset?>(locale, "image")
                    ?.urlForImageWith(
                        ImageOption.https(),
                        ImageOption.formatOf(ImageOption.Format.webp)
                    ).orEmpty()
            } catch (_: Throwable) {
                ""
            }
        )
    }

    data class Copy(
        val title: String,
        val copy: String
    ) : LessonModule() {
        constructor(entry: CDAEntry, locale: String) : this(
            entry.getField<String?>(locale, "title").orEmpty(),
            entry.getField<String?>(locale, "copy").orEmpty()
        )
    }
}
