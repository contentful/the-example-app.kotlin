package com.contentful.tea.kotlin.content

import com.contentful.java.cda.CDAClient
import com.contentful.java.cda.CDASpace
import com.contentful.tea.kotlin.content.rest.Contentful
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CountDownLatch
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class ContentfulTest {

    private lateinit var contentful: Contentful
    private lateinit var deliveryClient: CDAClient
    private lateinit var previewClient: CDAClient

    @Before
    fun setup() {
        deliveryClient = mock(CDAClient::class.java, "deliveryClient")
        `when`(deliveryClient.fetchSpace()).thenReturn(mock(CDASpace::class.java))

        previewClient = mock(CDAClient::class.java, "previewClient")
        `when`(previewClient.fetchSpace()).thenReturn(mock(CDASpace::class.java))

        contentful =
            object : Contentful(deliveryClient, previewClient, parameter = Parameter()) {
                override fun createClients(parameter: Parameter): Pair<CDAClient, CDAClient> =
                    Pair(deliveryClient, previewClient)

                override fun lookUpSuitableLocale() = parameter.locale!!
            }
    }

    @Test
    fun initializedContentfulHasEmptyParameter() {
        assertEquals("", contentful.parameter.deliveryToken)
        assertEquals("", contentful.parameter.previewToken)
        assertEquals("", contentful.parameter.spaceId)
        assertNull(contentful.parameter.api)
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
                assertEquals("en-US", contentful.parameter.locale)
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
                assertEquals("en-US", contentful.parameter.locale)
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
                assertEquals("en-US", contentful.parameter.locale)
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
    fun checkLocaleChanging() {
        val latch = CountDownLatch(1)
        val results = mutableListOf<Boolean>()

        contentful.applyParameter(Parameter(locale = "de-DE"),
            errorHandler = {
                latch.countDown()
                throw it
            },
            successHandler = {
                assertEquals("", contentful.parameter.deliveryToken)
                assertEquals("", contentful.parameter.previewToken)
                assertEquals("", contentful.parameter.spaceId)
                assertEquals("de-DE", contentful.parameter.locale)
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
                assertEquals("en-US", contentful.parameter.locale)
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
                assertEquals("en-US", contentful.parameter.locale)
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
    fun checkChangingHost() {
        val latch = CountDownLatch(1)
        val results = mutableListOf<Boolean>()

        contentful.applyParameter(Parameter(host = "example.com"),
            errorHandler = {
                latch.countDown()
                throw it
            },
            successHandler = {
                assertEquals("", contentful.parameter.deliveryToken)
                assertEquals("", contentful.parameter.previewToken)
                assertEquals("", contentful.parameter.spaceId)
                assertEquals("en-US", contentful.parameter.locale)
                assertEquals(Api.CDA, contentful.parameter.api)
                assertEquals("example.com", contentful.parameter.host)
                assertEquals(EditorialFeature.Disabled, contentful.parameter.editorialFeature)

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
            api = Api.CPA,
            locale = "fr-FR"
        ), errorHandler = {
            latch.countDown()
            throw it
        }, successHandler = {
            assertEquals("deliveryToken", contentful.parameter.deliveryToken)
            assertEquals("previewToken", contentful.parameter.previewToken)
            assertEquals("spaceId", contentful.parameter.spaceId)
            assertEquals("fr-FR", contentful.parameter.locale)
            assertEquals(Api.CPA, contentful.parameter.api)
            assertEquals(EditorialFeature.Enabled, contentful.parameter.editorialFeature)

            results.add(true)
            latch.countDown()
        })

        latch.await()

        assertEquals(1, results.size, "Parameters not changed, please check exceptions above.")
        assertTrue(results[0])
    }

    @Test
    fun checkCPAUsedWhenCPASet() {
        val results = mutableListOf<Boolean>()

        val applyLatch = CountDownLatch(1)
        contentful.applyParameter(
            Parameter(api = Api.CPA),
            errorHandler = {
                applyLatch.countDown()
                throw it
            }, successHandler = {
                results.add(true)
                applyLatch.countDown()
            })
        applyLatch.await()

        val fetchLatch = CountDownLatch(1)
        contentful.fetchSpace(
            errorCallback = {
                fetchLatch.countDown()
                throw it
            }, successCallback = {
                results.add(true)
                fetchLatch.countDown()
            })
        fetchLatch.await()

        assertEquals(2, results.size, "Not the right amount of invocations!")
        assertTrue(results[0])
        assertTrue(results[1])

        verify(deliveryClient).fetchSpace()
        verifyNoMoreInteractions(deliveryClient)

        verify(previewClient, times(2)).fetchSpace()
        verifyNoMoreInteractions(previewClient)
    }
}