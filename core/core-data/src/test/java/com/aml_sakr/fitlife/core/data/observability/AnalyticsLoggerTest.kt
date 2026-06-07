package com.aml_sakr.fitlife.core.data.observability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsLoggerTest {
    @Test
    fun inMemoryAnalyticsLoggerStoresEventsWithParameters() {
        val logger = InMemoryAnalyticsLogger()

        logger.logEvent(
            name = "setup_verified",
            params = mapOf(
                "source" to "unit_test",
                "attempt" to 1,
                "enabled" to true,
                "ignored" to null
            )
        )

        val event = logger.events.single()
        assertEquals("setup_verified", event.name)
        assertEquals("unit_test", event.params["source"])
        assertEquals(1, event.params["attempt"])
        assertEquals(true, event.params["enabled"])
        assertTrue("Null parameters should be dropped", "ignored" !in event.params)
    }

    @Test
    fun firebaseAnalyticsLoggerMapsSupportedParameterTypesAndDropsNulls() {
        val sink = CapturingAnalyticsSink()
        val logger = FirebaseAnalyticsLogger(sink)

        logger.logEvent(
            name = "setup_verified",
            params = mapOf(
                "source" to "unit_test",
                "attempt" to 1,
                "count" to 2L,
                "ratio" to 1.5f,
                "score" to 2.5,
                "enabled" to true,
                "fallback" to Any(),
                "ignored" to null
            )
        )

        assertEquals("setup_verified", sink.name)
        val values = sink.parameters.values
        assertEquals(FirebaseAnalyticsParameter.StringValue("unit_test"), values["source"])
        assertEquals(FirebaseAnalyticsParameter.LongValue(1L), values["attempt"])
        assertEquals(FirebaseAnalyticsParameter.LongValue(2L), values["count"])
        assertEquals(FirebaseAnalyticsParameter.DoubleValue(1.5), values["ratio"])
        assertEquals(FirebaseAnalyticsParameter.DoubleValue(2.5), values["score"])
        assertEquals(FirebaseAnalyticsParameter.StringValue("true"), values["enabled"])
        assertTrue(values["fallback"] is FirebaseAnalyticsParameter.StringValue)
        assertTrue("Null parameters should be dropped", "ignored" !in values)
    }

    private class CapturingAnalyticsSink : FirebaseAnalyticsEventSink {
        lateinit var name: String
        lateinit var parameters: FirebaseAnalyticsParameters

        override fun logEvent(name: String, parameters: FirebaseAnalyticsParameters) {
            this.name = name
            this.parameters = parameters
        }
    }
}
