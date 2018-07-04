package com.contentful.tea.kotlin.data

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
        entry.getField<String>(locale, "title"),
        entry.getField<String>(locale, "slug"),
        entry.getField<List<CDAEntry>>(locale, "contentModules")
            .map { findLayoutModule(it, locale) }
    )
}

fun findLayoutModule(entry: CDAEntry, locale: String): LayoutModule =
    when (entry.contentType().id()) {
        "layoutHighlightedCourse" -> LayoutModule.HightlightedCourse(entry, locale)
        "layoutCopy" -> LayoutModule.Copy(entry, locale)
        "layoutHeroImage" -> LayoutModule.HeroImage(entry, locale)
        else -> LayoutModule.Copy(
            "<layout module type not found>",
            "##layout module type not found",
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
            entry.getField<String>(locale, "title"),
            entry.getField<String>(locale, "headline"),
            entry.getField<String>(locale, "copy"),
            entry.getField<String>(locale, "ctaTitle"),
            entry.getField<String>(locale, "ctaLink"),
            entry.getField<String>(locale, "visualStyle")
        )
    }

    data class HeroImage(
        val title: String,
        val headline: String,
        val backgroundImage: String

    ) : LayoutModule() {
        constructor(entry: CDAEntry, locale: String) : this(
            entry.getField<String>(locale, "title"),
            entry.getField<String>(locale, "headline"),
            entry.getField<CDAAsset>(locale, "backgroundImage")
                .urlForImageWith(https(), formatOf(webp))
        )
    }

    data class HightlightedCourse(
        val title: String,
        val course: Course
    ) : LayoutModule() {
        constructor(entry: CDAEntry, locale: String) : this(
            entry.getField<String>(locale, "title"),
            Course(entry.getField<CDAEntry>(locale, "course"), locale)
        )
    }
}