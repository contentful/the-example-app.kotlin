@file:JvmName("FOO")

package com.contentful.tea.kotlin.routing

import com.contentful.tea.kotlin.content.Api.CPA
import com.contentful.tea.kotlin.content.EditorialFeature.Enabled
import com.contentful.tea.kotlin.content.Parameter
import com.contentful.tea.kotlin.separateParameterFromPath
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import kotlin.test.assertEquals

class IntentRouterTest {

    @Test
    fun testHomeRoute() {
        val callback = mock(RouteCallback::class.java)

        route("", Parameter(), callback)

        verify(callback).openHome(any())
        verifyNoMoreInteractions(callback)
    }

    @Test
    fun testGarbageRoute() {
        val callback = mock(RouteCallback::class.java)

        route("garbage", Parameter(), callback)

        verifyNoMoreInteractions(callback)
    }

    @Test
    fun testCoursesRoute() {
        val callback = mock(RouteCallback::class.java)

        route("courses/someCourseId", Parameter(), callback)

        verify(callback).goToCourse(eq("someCourseId"), any())
        verifyNoMoreInteractions(callback)
    }

    @Test
    fun testCategoryRoute() {
        val callback = mock(RouteCallback::class.java)

        route("courses/categories/someCategoryId", Parameter(), callback)

        verify(callback).goToCategory(eq("someCategoryId"), any())
        verifyNoMoreInteractions(callback)
    }

    @Test
    fun testLessonRoute() {
        val callback = mock(RouteCallback::class.java)

        route("courses/someCourseId/lessons/someLessonId", Parameter(), callback)

        verify(callback).goToLesson(eq("someCourseId"), eq("someLessonId"), any())
        verifyNoMoreInteractions(callback)
    }

    @Test
    fun testParameterParsing() {
        val (parameter, path) = separateParameterFromPath(
            "?space_id=spaceId&" +
                "preview_token=previewToken&" +
                "delivery_token=deliveryToken&" +
                "editorial_features=enabled&" +
                "locale=fr-FR&" +
                "host=example.com&" +
                "api=CPA"
        )

        assertEquals(
            parameter,
            Parameter(
                "spaceId",
                "previewToken",
                "deliveryToken",
                Enabled,
                CPA,
                "fr-FR",
                "example.com"
            )
        )
        assertEquals("", path)
    }

    @Test
    fun testNoParameterGiven() {
        val callback = mock(RouteCallback::class.java)

        route("", Parameter(), callback)

        verify(callback).openHome(eq(Parameter()))
        verifyNoMoreInteractions(callback)
    }

    /*
     * The following methods are here for interop between kotlin and mockito.
     * @see https://medium.com/@elye.project/befriending-kotlin-and-mockito-1c2e7b0ef791
     */

    private fun <T> any(): T {
        Mockito.any<T>()
        return uninitialized()
    }

    private fun <T> eq(t: T): T {
        return Mockito.eq<T>(t)
    }

    private fun <T> uninitialized(): T = null as T
}