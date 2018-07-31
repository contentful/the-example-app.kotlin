package com.contentful.tea.kotlin.contentful

import com.contentful.java.cda.CDAAsset
import com.contentful.java.cda.CDAEntry
import com.contentful.java.cda.image.ImageOption.Format.webp
import com.contentful.java.cda.image.ImageOption.formatOf
import com.contentful.java.cda.image.ImageOption.https

data class Layout(
    val title: String,
    val slug: String,
    val contentModules: List<LayoutModule>
) {
    constructor(entry: CDAEntry, locale: String) : this(
        entry.getField<String?>(locale, "title").orEmpty(),
        entry.getField<String?>(locale, "slug").orEmpty(),
        entry.getField<List<CDAEntry>?>(locale, "contentModules").orEmpty().map {
            findLayoutModule(it, locale)
        }
    )
}

fun findLayoutModule(entry: CDAEntry, locale: String): LayoutModule =
    when (entry.contentType().id()) {
        "layoutHighlightedCourse" -> LayoutModule.HightlightedCourse(entry, locale)
        "layoutCopy" -> LayoutModule.Copy(entry, locale)
        "layoutHeroImage" -> LayoutModule.HeroImage(entry, locale)
        else -> LayoutModule.Copy(
            "<layout module type not found>",
            "## layout module type not found",
            "",
            "",
            "",
            ""
        )
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
        constructor(entry: CDAEntry, locale: String) : this(
            entry.getField<String?>(locale, "title").orEmpty(),
            entry.getField<String?>(locale, "headline").orEmpty(),
            entry.getField<String?>(locale, "copy").orEmpty(),
            entry.getField<String?>(locale, "ctaTitle").orEmpty(),
            entry.getField<String?>(locale, "ctaLink").orEmpty(),
            entry.getField<String?>(locale, "visualStyle").orEmpty()
        )
    }

    data class HeroImage(
        val title: String,
        val headline: String,
        val backgroundImage: String

    ) : LayoutModule() {
        constructor(entry: CDAEntry, locale: String) : this(
            entry.getField<String?>(locale, "title").orEmpty(),
            entry.getField<String?>(locale, "headline").orEmpty(),
            entry.getField<CDAAsset?>(locale, "backgroundImage")?.urlForImageWith(
                https(),
                formatOf(webp)
            ).orEmpty()
        )
    }

    data class HightlightedCourse(
        val title: String,
        val course: Course
    ) : LayoutModule() {
        constructor(entry: CDAEntry, locale: String) : this(
            entry.getField<String?>(locale, "title").orEmpty(),
            if (entry.getField<CDAEntry?>(locale, "course") == null) {
                Course(
                    "", "", "", "", "", 0, "",
                    emptyList(), emptyList()
                )
            } else {
                Course(entry.getField<CDAEntry>(locale, "course"), locale)
            }
        )
    }
}