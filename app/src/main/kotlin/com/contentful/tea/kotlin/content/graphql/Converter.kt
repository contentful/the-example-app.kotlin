package com.contentful.tea.kotlin.content.graphql

import com.contentful.tea.kotlin.content.Category
import com.contentful.tea.kotlin.content.Course
import com.contentful.tea.kotlin.content.Layout
import com.contentful.tea.kotlin.content.LayoutModule
import com.contentful.tea.kotlin.content.Lesson
import com.contentful.tea.kotlin.content.LessonModule
import com.contentful.tea.kotlin.content.graphql.fragment.Lesson.AsLessonCodeSnippets
import com.contentful.tea.kotlin.content.graphql.fragment.Lesson.AsLessonCopy
import com.contentful.tea.kotlin.content.graphql.fragment.Lesson.AsLessonImage

fun Layout.Companion.fromGraphQlEntry(home: HomeQuery.Data) =
    with(home.layoutCollection?.items.orEmpty().first()) {
        Layout(
            title.orEmpty(),
            slug.orEmpty(),
            contentModulesCollection?.items.orEmpty().map { module ->
                createHomeModule(module)
            }
        )
    }

fun createHomeModule(module: HomeQuery.Item1): LayoutModule =
    when (module) {
        is HomeQuery.AsLayoutHighlightedCourse -> LayoutModule.HightlightedCourse.fromGraphQlEntry(
            module
        )
        is HomeQuery.AsLayoutCopy -> LayoutModule.Copy.fromGraphQlEntry(module)
        is HomeQuery.AsLayoutHeroImage -> LayoutModule.HeroImage.fromGraphQlEntry(module)
        else -> LayoutModule.Copy(
            "<layout module type not found>",
            "## layout module type not found",
            "",
            "",
            "",
            ""
        )
    }

fun LayoutModule.HightlightedCourse.Companion.fromGraphQlEntry(
    course: HomeQuery.AsLayoutHighlightedCourse
) = LayoutModule.HightlightedCourse(
    course.course?.fragments()?.hightlightedCourse?.title().orEmpty(),
    Course(
        course.course?.fragments()?.hightlightedCourse?.title().orEmpty(),
        course.course?.fragments()?.hightlightedCourse?.slug().orEmpty(),
        course.course?.fragments()?.hightlightedCourse?.image()?.url().orEmpty(),
        course.course?.fragments()?.hightlightedCourse?.shortDescription().orEmpty(),
        "",
        0,
        "",
        emptyList(),
        emptyList()
    )
)

fun LayoutModule.HeroImage.Companion.fromGraphQlEntry(hero: HomeQuery.AsLayoutHeroImage) =
    LayoutModule.HeroImage(
        hero.title.orEmpty(),
        hero.headline.orEmpty(),
        hero.backgroundImage?.url.orEmpty()
    )

fun LayoutModule.Copy.Companion.fromGraphQlEntry(copy: HomeQuery.AsLayoutCopy) =
    LayoutModule.Copy(
        copy.title.orEmpty(),
        copy.headline.orEmpty(),
        copy.copy.orEmpty(),
        copy.ctaTitle.orEmpty(),
        copy.ctaLink.orEmpty(),
        copy.visualStyle.orEmpty()
    )

fun Course.Companion.fromGraphQlEntry(
    course: com.contentful.tea.kotlin.content.graphql.fragment.Course
): Course =
    Course(
        course.title().orEmpty(),
        course.slug().orEmpty(),
        course.image()?.url().orEmpty(),
        course.shortDescription().orEmpty(),
        course.description().orEmpty(),
        course.duration().or(0),
        course.skillLevel().orEmpty(),
        course.lessonsCollection()?.items().orEmpty().map {
            Lesson.fromGraphQlEntry(it.fragments().lesson())
        },
        course.categoriesCollection()?.items().orEmpty().map {
            Category.fromGraphQlEntry(it.fragments().category())
        }
    )

private fun Int?.or(default: Int): Int = this ?: default

fun Lesson.Companion.fromGraphQlEntry(
    lesson: com.contentful.tea.kotlin.content.graphql.fragment.Lesson
) =
    Lesson(
        lesson.title().orEmpty(),
        lesson.slug().orEmpty(),
        lesson.modulesCollection()?.items().orEmpty().map { findLessonModule(it) }
    )

fun findLessonModule(
    module: com.contentful.tea.kotlin.content.graphql.fragment.Lesson.Item
): LessonModule =
    when (module) {
        is AsLessonCodeSnippets -> LessonModule.CodeSnippet.fromGraphQlEntry(module)
        is AsLessonImage -> LessonModule.Image.fromGraphQlEntry(module)
        is AsLessonCopy -> LessonModule.Copy.fromGraphQlEntry(module)
        else -> LessonModule.Copy(
            "<lesson module type not found>",
            "## lesson module type not found"
        )
    }

fun LessonModule.CodeSnippet.Companion.fromGraphQlEntry(module: AsLessonCodeSnippets) =
    with(module.fragments().lessonCodeSnippet()!!) {
        LessonModule.CodeSnippet(
            title().orEmpty(),
            curl().orEmpty(),
            dotNet().orEmpty(),
            javascript().orEmpty(),
            java().orEmpty(),
            javaAndroid().orEmpty(),
            php().orEmpty(),
            python().orEmpty(),
            ruby().orEmpty(),
            swift().orEmpty()
        )
    }

fun LessonModule.Image.Companion.fromGraphQlEntry(module: AsLessonImage) =
    with(module.fragments().lessonImage()!!) {
        LessonModule.Image(
            title().orEmpty(),
            caption().orEmpty(),
            image()?.url().orEmpty()
        )
    }

fun LessonModule.Copy.Companion.fromGraphQlEntry(module: AsLessonCopy) =
    with(module.fragments().lessonCopy()!!) {
        LessonModule.Copy(
            title().orEmpty(),
            copy().orEmpty()
        )
    }

fun Category.Companion.fromGraphQlEntry(
    category: com.contentful.tea.kotlin.content.graphql.fragment.Category
) =
    Category(
        category.sys().id().orEmpty(),
        category.title().orEmpty(),
        category.slug().orEmpty()
    )