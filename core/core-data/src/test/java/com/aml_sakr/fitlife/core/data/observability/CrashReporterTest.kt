package com.aml_sakr.fitlife.core.data.observability

import org.junit.Assert.assertEquals
import org.junit.Test

class CrashReporterTest {
    @Test
    fun inMemoryCrashReporterStoresNonFatalExceptionsWithKeys() {
        val reporter = InMemoryCrashReporter()
        val throwable = IllegalStateException("boom")

        reporter.recordException(
            throwable = throwable,
            keys = mapOf("screen" to "setup")
        )

        val report = reporter.exceptions.single()
        assertEquals(throwable, report.throwable)
        assertEquals("setup", report.keys["screen"])
    }

    @Test
    fun firebaseCrashReporterSetsKeysBeforeRecordingException() {
        val sink = CapturingCrashlyticsSink()
        val reporter = FirebaseCrashReporter(sink)
        val throwable = IllegalArgumentException("non fatal")

        reporter.recordException(
            throwable = throwable,
            keys = mapOf(
                "screen" to "setup",
                "flow" to "firebase"
            )
        )

        assertEquals(
            listOf(
                CrashlyticsCall.SetCustomKey("screen", "setup"),
                CrashlyticsCall.SetCustomKey("flow", "firebase"),
                CrashlyticsCall.RecordException(throwable)
            ),
            sink.calls
        )
    }

    private class CapturingCrashlyticsSink : FirebaseCrashlyticsSink {
        val calls = mutableListOf<CrashlyticsCall>()

        override fun setCustomKey(key: String, value: String) {
            calls += CrashlyticsCall.SetCustomKey(key, value)
        }

        override fun recordException(throwable: Throwable) {
            calls += CrashlyticsCall.RecordException(throwable)
        }
    }

    private sealed interface CrashlyticsCall {
        data class SetCustomKey(val key: String, val value: String) : CrashlyticsCall
        data class RecordException(val throwable: Throwable) : CrashlyticsCall
    }
}
