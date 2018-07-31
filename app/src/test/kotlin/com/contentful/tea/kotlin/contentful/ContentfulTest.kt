package com.contentful.tea.kotlin.contentful

import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDASpace
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class ContentfulTest {

    private lateinit var contentful: Contentful

    @Before
    fun setup() {
        val client = mock(CDAClient::class.java)

        contentful = object : Contentful(client, Parameter()) {
            override fun createClient(parameter: Parameter): CDAClient =
                mock(CDAClient::class.java).apply {
                    `when`(fetchSpace()).thenReturn(mock(CDASpace::class.java))
                }
        }
    }

    @Test
    fun initializedContentfulHasEmptyParameter() {
        assertEquals("", contentful.parameter.deliveryToken)
        assertEquals("", contentful.parameter.previewToken)
        assertEquals("", contentful.parameter.spaceId)
        assertEquals(Api.CDA, contentful.parameter.api)
        assertEquals(EditorialFeature.Disabled, contentful.parameter.editorialFeature)
    }

    @Test
    fun checkChangingDeliveryTokenOnly() {
        val latch = CountDownLatch(1)
        val results = mutableListOf<Boolean>()

        contentful.applyParameter(Parameter(deliveryToken = "deliveryToken"),
            errorHandler = {
                latch.countDown()
                throw it
            },
            successHandler = {
                assertEquals("deliveryToken", contentful.parameter.deliveryToken)
                assertEquals("", contentful.parameter.previewToken)
                assertEquals("", contentful.parameter.spaceId)
                assertEquals(Api.CDA, contentful.parameter.api)
                assertEquals(EditorialFeature.Disabled, contentful.parameter.editorialFeature)

                results.add(true)
                latch.countDown()
            })

        latch.await()

        assertEquals(1, results.size, "Parameters not changed, please check exceptions above.")
        assertTrue(results[0])
    }

    @Test
    fun checkChangingPreviewTokenOnly() {
        val latch = CountDownLatch(1)
        val results = mutableListOf<Boolean>()

        contentful.applyParameter(Parameter(previewToken = "previewToken"),
            errorHandler = {
                latch.countDown()
                throw it
            },
            successHandler = {
                assertEquals("", contentful.parameter.deliveryToken)
                assertEquals("previewToken", contentful.parameter.previewToken)
                assertEquals("", contentful.parameter.spaceId)
                assertEquals(Api.CDA, contentful.parameter.api)
                assertEquals(EditorialFeature.Disabled, contentful.parameter.editorialFeature)

                results.add(true)
                latch.countDown()
            })

        latch.await()

        assertEquals(1, results.size, "Parameters not changed, please check exceptions above.")
        assertTrue(results[0])
    }

    @Test
    fun checkSpaceIdChanging() {
        val latch = CountDownLatch(1)
        val results = mutableListOf<Boolean>()

        contentful.applyParameter(Parameter(spaceId = "spaceId"),
            errorHandler = {
                latch.countDown()
                throw it
            },
            successHandler = {
                assertEquals("", contentful.parameter.deliveryToken)
                assertEquals("", contentful.parameter.previewToken)
                assertEquals("spaceId", contentful.parameter.spaceId)
                assertEquals(Api.CDA, contentful.parameter.api)
                assertEquals(EditorialFeature.Disabled, contentful.parameter.editorialFeature)

                results.add(true)
                latch.countDown()
            })

        latch.await()

        assertEquals(1, results.size, "Parameters not changed, please check exceptions above.")
        assertTrue(results[0])
    }

    @Test
    fun checkChangingApi() {
        val latch = CountDownLatch(1)
        val results = mutableListOf<Boolean>()

        contentful.applyParameter(Parameter(api = Api.CPA),
            errorHandler = {
                latch.countDown()
                throw it
            },
            successHandler = {
                assertEquals("", contentful.parameter.deliveryToken)
                assertEquals("", contentful.parameter.previewToken)
                assertEquals("", contentful.parameter.spaceId)
                assertEquals(Api.CPA, contentful.parameter.api)
                assertEquals(EditorialFeature.Disabled, contentful.parameter.editorialFeature)

                results.add(true)
                latch.countDown()
            })

        latch.await()

        assertEquals(1, results.size, "Parameters not changed, please check exceptions above.")
        assertTrue(results[0])
    }

    @Test
    fun checkChangingEditorialFeatures() {
        val latch = CountDownLatch(1)
        val results = mutableListOf<Boolean>()

        contentful.applyParameter(Parameter(editorialFeature = EditorialFeature.Enabled),
            errorHandler = {
                latch.countDown()
                throw it
            },
            successHandler = {
                assertEquals("", contentful.parameter.deliveryToken)
                assertEquals("", contentful.parameter.previewToken)
                assertEquals("", contentful.parameter.spaceId)
                assertEquals(Api.CDA, contentful.parameter.api)
                assertEquals(EditorialFeature.Enabled, contentful.parameter.editorialFeature)

                results.add(true)
                latch.countDown()
            })

        latch.await()

        assertEquals(1, results.size, "Parameters not changed, please check exceptions above.")
        assertTrue(results[0])
    }

    @Test
    fun checkChangingAllSettings() {
        val latch = CountDownLatch(1)
        val results = mutableListOf<Boolean>()

        contentful.applyParameter(Parameter(
            spaceId = "spaceId",
            previewToken = "previewToken",
            deliveryToken = "deliveryToken",
            editorialFeature = EditorialFeature.Enabled,
            api = Api.CPA
        ), errorHandler = {
            latch.countDown()
            throw it
        }, successHandler = {
            assertEquals("deliveryToken", contentful.parameter.deliveryToken)
            assertEquals("previewToken", contentful.parameter.previewToken)
            assertEquals("spaceId", contentful.parameter.spaceId)
            assertEquals(Api.CPA, contentful.parameter.api)
            assertEquals(EditorialFeature.Enabled, contentful.parameter.editorialFeature)

            results.add(true)
            latch.countDown()
        })

        latch.await()

        assertEquals(1, results.size, "Parameters not changed, please check exceptions above.")
        assertTrue(results[0])
    }
}