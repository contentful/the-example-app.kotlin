package com.contentful.tea.kotlin.content.graphql

import com.contentful.tea.kotlin.content.Category
import com.contentful.tea.kotlin.content.Course
import com.contentful.tea.kotlin.content.Layout
import com.contentful.tea.kotlin.content.LayoutModule
import com.contentful.tea.kotlin.content.Lesson
import com.contentful.tea.kotlin.content.LessonModule
import com.contentful.tea.kotlin.content.graphql.fragment.LessonCodeSnippetFragment
import com.contentful.tea.kotlin.content.graphql.fragment.LessonCopyFragment
import com.contentful.tea.kotlin.content.graphql.fragment.LessonImageFragment

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
    course.course?.fragments()?.hightlightedCourseFragment?.title().orEmpty(),
    Course(
        course.course?.fragments()?.hightlightedCourseFragment?.title().orEmpty(),
        course.course?.fragments()?.hightlightedCourseFragment?.slug().orEmpty(),
        course.course?.fragments()?.hightlightedCourseFragment?.image()?.url().orEmpty(),
        course.course?.fragments()?.hightlightedCourseFragment?.shortDescription().orEmpty(),
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
    course: com.contentful.tea.kotlin.content.graphql.fragment.CourseFragment
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
            Lesson.fromGraphQlEntry(it.fragments().lessonFragment())
        },
        course.categoriesCollection()?.items().orEmpty().map {
            Category.fromGraphQlEntry(it.fragments().categoryFragment())
        }
    )

private fun Int?.or(default: Int): Int = this ?: default

fun Lesson.Companion.fromGraphQlEntry(
    lesson: com.contentful.tea.kotlin.content.graphql.fragment.LessonFragment
) =
    Lesson(
        lesson.title().orEmpty(),
        lesson.slug().orEmpty(),
        lesson.modulesCollection()?.items().orEmpty().map { findLessonModule(it) }
    )

fun findLessonModule(
    module: com.contentful.tea.kotlin.content.graphql.fragment.LessonFragment.Item
): LessonModule =
    when (module.__typename()) {
        "LessonCodeSnippets" -> LessonModule.CodeSnippet.fromGraphQlEntry(
            module.fragments().lessonCodeSnippetFragment()!!
        )
        "LessonImage" -> LessonModule.Image.fromGraphQlEntry(
            module.fragments().lessonImageFragment()!!
        )
        "LessonCopy" -> LessonModule.Copy.fromGraphQlEntry(
            module.fragments().lessonCopyFragment()!!
        )
        else -> LessonModule.Copy(
            "<lesson module type not found>",
            "## lesson module type not found"
        )
    }

fun LessonModule.CodeSnippet.Companion.fromGraphQlEntry(module: LessonCodeSnippetFragment) =
    LessonModule.CodeSnippet(
        module.title().orEmpty(),
        module.curl().orEmpty(),
        module.dotNet().orEmpty(),
        module.javascript().orEmpty(),
        module.java().orEmpty(),
        module.javaAndroid().orEmpty(),
        module.php().orEmpty(),
        module.python().orEmpty(),
        module.ruby().orEmpty(),
        module.swift().orEmpty()
    )

fun LessonModule.Image.Companion.fromGraphQlEntry(module: LessonImageFragment) =
    LessonModule.Image(
        module.title().orEmpty(),
        module.caption().orEmpty(),
        module.image()?.url().orEmpty()
    )

fun LessonModule.Copy.Companion.fromGraphQlEntry(module: LessonCopyFragment) =
    LessonModule.Copy(
        module.title().orEmpty(),
        module.copy().orEmpty()
    )

fun Category.Companion.fromGraphQlEntry(
    category: com.contentful.tea.kotlin.content.graphql.fragment.CategoryFragment
) =
    Category(
        category.sys().id().orEmpty(),
        category.title().orEmpty(),
        category.slug().orEmpty()
    )