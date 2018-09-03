package com.contentful.tea.kotlin.content.rest

import com.contentful.java.cda.CDAAsset
import com.contentful.java.cda.CDAEntry
import com.contentful.java.cda.image.ImageOption
import com.contentful.java.cda.image.ImageOption.formatOf
import com.contentful.java.cda.image.ImageOption.https
import com.contentful.tea.kotlin.content.Category
import com.contentful.tea.kotlin.content.Course
import com.contentful.tea.kotlin.content.Layout
import com.contentful.tea.kotlin.content.LayoutModule
import com.contentful.tea.kotlin.content.Lesson
import com.contentful.tea.kotlin.content.LessonModule

fun Course.Companion.fromRestEntry(entry: CDAEntry, locale: String): Course = Course(
    entry.getField<String?>(locale, "title").orEmpty(),
    entry.getField<String?>(locale, "slug").orEmpty(),
    try {
        entry.getField<CDAAsset?>(locale, "image")
            ?.urlForImageWith(ImageOption.https(), ImageOption.formatOf(ImageOption.Format.webp))
            .orEmpty()
    } catch (_: Throwable) {
        ""
    },
    entry.getField<String?>(locale, "shortDescription").orEmpty(),
    entry.getField<String?>(locale, "description").orEmpty(),
    entry.getField<Double?>(locale, "duration").or(0.0).toInt(),
    entry.getField<String?>(locale, "skillLevel").orEmpty(),
    entry.getField<List<CDAEntry>?>(locale, "lessons")
        .orEmpty()
        .map { Lesson.fromRestEntry(it, locale) },
    entry.getField<List<CDAEntry>>(locale, "categories")
        .orEmpty()
        .map { Category.fromRestEntry(it, locale) }
)

private fun Double?.or(default: Double): Double = this ?: default

fun Category.Companion.fromRestEntry(entry: CDAEntry, locale: String) = Category(
    entry.id(),
    entry.getField<String?>(locale, "title").orEmpty(),
    entry.getField<String?>(locale, "slug").orEmpty()
)

fun Layout.Companion.fromRestEntry(entry: CDAEntry, locale: String) = Layout(
    entry.getField<String?>(locale, "title").orEmpty(),
    entry.getField<String?>(locale, "slug").orEmpty(),
    entry.getField<List<CDAEntry>?>(locale, "contentModules").orEmpty().map {
        findLayoutModule(it, locale)
    }
)

fun findLayoutModule(entry: CDAEntry, locale: String): LayoutModule =
    when (entry.contentType().id()) {
        "layoutHighlightedCourse" -> LayoutModule.HightlightedCourse.fromRestEntry(entry, locale)
        "layoutCopy" -> LayoutModule.Copy.fromRestEntry(entry, locale)
        "layoutHeroImage" -> LayoutModule.HeroImage.fromRestEntry(entry, locale)
        else -> LayoutModule.Copy(
            "<layout module type not found>",
            "## layout module type not found",
            "",
            "",
            "",
            ""
        )
    }

fun LayoutModule.HightlightedCourse.Companion.fromRestEntry(entry: CDAEntry, locale: String) =
    LayoutModule.HightlightedCourse(
        entry.getField<String?>(locale, "title").orEmpty(),
        if (entry.getField<CDAEntry?>(locale, "course") == null) {
            Course(
                "", "", "", "", "", 0, "",
                emptyList(), emptyList()
            )
        } else {
            Course.fromRestEntry(entry.getField<CDAEntry>(locale, "course"), locale)
        }
    )

fun LayoutModule.HeroImage.Companion.fromRestEntry(entry: CDAEntry, locale: String) =
    LayoutModule.HeroImage(
        entry.getField<String?>(locale, "title").orEmpty(),
        entry.getField<String?>(locale, "headline").orEmpty(),
        try {
            entry.getField<CDAAsset?>(locale, "backgroundImage")
                ?.urlForImageWith(https(), formatOf(ImageOption.Format.webp)).orEmpty()
        } catch (_: Throwable) {
            ""
        }
    )

fun LayoutModule.Copy.Companion.fromRestEntry(entry: CDAEntry, locale: String) =
    LayoutModule.Copy(
        entry.getField<String?>(locale, "title").orEmpty(),
        entry.getField<String?>(locale, "headline").orEmpty(),
        entry.getField<String?>(locale, "copy").orEmpty(),
        entry.getField<String?>(locale, "ctaTitle").orEmpty(),
        entry.getField<String?>(locale, "ctaLink").orEmpty(),
        entry.getField<String?>(locale, "visualStyle").orEmpty()
    )

fun Lesson.Companion.fromRestEntry(entry: CDAEntry, locale: String) = Lesson(
    entry.getField<String?>(locale, "title").orEmpty(),
    entry.getField<String?>(locale, "slug").orEmpty(),
    entry.getField<List<CDAEntry>?>(locale, "modules")
        .orEmpty()
        .map { findLessonModule(it, locale) }
)

fun findLessonModule(entry: CDAEntry, locale: String): LessonModule =
    when (entry.contentType().id()) {
        "lessonCodeSnippets" -> LessonModule.CodeSnippet.fromRestEntry(entry, locale)
        "lessonImage" -> LessonModule.Image.fromRestEntry(entry, locale)
        "lessonCopy" -> LessonModule.Copy.fromRestEntry(entry, locale)
        else -> LessonModule.Copy(
            "<lesson module type not found>",
            "## lesson module type not found"
        )
    }

fun LessonModule.CodeSnippet.Companion.fromRestEntry(entry: CDAEntry, locale: String) =
    LessonModule.CodeSnippet(
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

fun LessonModule.Image.Companion.fromRestEntry(entry: CDAEntry, locale: String) =
    LessonModule.Image(
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

fun LessonModule.Copy.Companion.fromRestEntry(entry: CDAEntry, locale: String) = LessonModule.Copy(
    entry.getField<String?>(locale, "title").orEmpty(),
    entry.getField<String?>(locale, "copy").orEmpty()
)