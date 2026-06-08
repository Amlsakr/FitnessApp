package com.aml_sakr.fitlife.feature.workout.data.gemini

import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkConfiguration
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkEnvironment
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkExecutionTarget
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkProfiles
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiCallStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.SocketTimeoutException

class GeminiLatencyBenchmarkRunnerTest {

    @Test
    fun `runner records successful parsed responses and timeout fallback`() = runTest {
        val service = FakeGeminiApiService(
            results = listOf(
                GeminiApiCallResult(
                    httpStatusCode = 200,
                    responseBody = generateContentResponse(validPlanJson()),
                    responseSizeChars = 1200
                ),
                SocketTimeoutException("timed out")
            )
        )
        val runner = GeminiLatencyBenchmarkRunner(
            apiService = service,
            promptBuilder = GeminiWorkoutPromptBuilder(),
            responseParser = GeminiPlanResponseParser(),
            clock = SequenceBenchmarkClock(0L, 1_500L, 1_540L, 2_000L, 7_000L)
        )

        val run = runner.run(
            apiKey = "secret-key-that-must-not-be-recorded",
            environment = environment(usedLiveGeminiApi = false, quotaVerified = false),
            configuration = configuration(),
            profiles = GeminiBenchmarkProfiles.representativeProfiles()
        )

        assertEquals(2, run.samples.size)
        assertEquals(GeminiCallStatus.Success, run.samples[0].status)
        assertEquals(1_540L, run.samples[0].totalLatencyMillis)
        assertEquals(40L, run.samples[0].parsingLatencyMillis)
        assertTrue(run.samples[0].schemaValid)
        assertTrue(run.samples[0].mappedToWorkoutPlan)
        assertFalse(run.samples[0].fallbackUsed)
        assertEquals(GeminiCallStatus.Timeout, run.samples[1].status)
        assertTrue(run.samples[1].fallbackUsed)
        assertEquals("assets/fallback_workout_plans.json", run.samples[1].fallbackPlanPath)
        assertFalse(run.toString().contains("secret-key-that-must-not-be-recorded"))
    }

    @Test
    fun `runner classifies non success http responses before parsing`() = runTest {
        val service = FakeGeminiApiService(
            results = listOf(
                GeminiApiCallResult(
                    httpStatusCode = 429,
                    responseBody = """{"error":{"message":"quota exceeded"}}""",
                    responseSizeChars = 38
                ),
                GeminiApiCallResult(
                    httpStatusCode = 500,
                    responseBody = """{"error":{"message":"server error"}}""",
                    responseSizeChars = 37
                )
            )
        )
        val runner = GeminiLatencyBenchmarkRunner(
            apiService = service,
            promptBuilder = GeminiWorkoutPromptBuilder(),
            responseParser = GeminiPlanResponseParser(),
            clock = SequenceBenchmarkClock(0L, 100L, 200L, 350L)
        )

        val run = runner.run(
            apiKey = "secret-key-that-must-not-be-recorded",
            environment = environment(usedLiveGeminiApi = false, quotaVerified = false),
            configuration = configuration(),
            profiles = GeminiBenchmarkProfiles.representativeProfiles()
        )

        assertEquals(GeminiCallStatus.RateLimited, run.samples[0].status)
        assertEquals("rate_limited", run.samples[0].errorCategory)
        assertEquals(GeminiCallStatus.HttpError, run.samples[1].status)
        assertEquals("http_error", run.samples[1].errorCategory)
    }

    @Test
    fun `runner treats end to end parsing over timeout as timeout`() = runTest {
        val service = FakeGeminiApiService(
            results = listOf(
                GeminiApiCallResult(
                    httpStatusCode = 200,
                    responseBody = generateContentResponse(validPlanJson()),
                    responseSizeChars = 1200
                )
            )
        )
        val runner = GeminiLatencyBenchmarkRunner(
            apiService = service,
            promptBuilder = GeminiWorkoutPromptBuilder(),
            responseParser = GeminiPlanResponseParser(),
            clock = SequenceBenchmarkClock(0L, 4_990L, 5_010L)
        )

        val run = runner.run(
            apiKey = "secret-key-that-must-not-be-recorded",
            environment = environment(usedLiveGeminiApi = false, quotaVerified = false),
            configuration = configuration(),
            profiles = GeminiBenchmarkProfiles.representativeProfiles().take(1)
        )

        assertEquals(GeminiCallStatus.Timeout, run.samples.single().status)
        assertTrue(run.samples.single().fallbackUsed)
    }

    @Test
    fun `prompt builder requests structured json for seven day workout plans`() {
        val request = GeminiWorkoutPromptBuilder().buildRequest(
            profile = GeminiBenchmarkProfiles.representativeProfiles().first(),
            configuration = configuration()
        )

        assertEquals("application/json", request.generationConfig.responseMimeType)
        assertEquals(0.2, request.generationConfig.temperature, 0.001)
        assertEquals(4096, request.generationConfig.maxOutputTokens)
        assertTrue(request.contents.first().parts.first().text.contains("7-day workout plan"))
        assertTrue(request.generationConfig.responseSchema.required.contains("days"))
    }

    @Test
    fun `parser accepts valid workout plan and rejects malformed plan`() {
        val parser = GeminiPlanResponseParser()

        val valid = parser.parse(generateContentResponse(validPlanJson()))
        val invalid = parser.parse(generateContentResponse("""{"days":[]}"""))
        val duplicateDays = parser.parse(generateContentResponse(duplicateDayPlanJson()))

        assertTrue(valid.isValidPlan)
        assertEquals(7, valid.plan?.days?.size)
        assertFalse(invalid.isValidPlan)
        assertFalse(duplicateDays.isValidPlan)
    }

    private fun environment(
        usedLiveGeminiApi: Boolean,
        quotaVerified: Boolean
    ) = GeminiBenchmarkEnvironment(
        deviceModel = "local test",
        androidApiLevel = null,
        networkType = "fake",
        approximateRegion = "test",
        runTimestamp = "2026-06-07T00:00:00+03:00",
        executionTarget = GeminiBenchmarkExecutionTarget.LocalJvm,
        quotaVerified = quotaVerified,
        usedLiveGeminiApi = usedLiveGeminiApi
    )

    private fun configuration() = GeminiBenchmarkConfiguration(
        modelName = "models/gemini-2.5-flash-lite",
        apiVersion = "v1beta",
        endpoint = "v1beta/models/gemini-2.5-flash-lite:generateContent",
        responseMimeType = "application/json",
        temperature = 0.2,
        maxOutputTokens = 4096,
        timeoutMillis = 5_000L,
        maxRetries = 0
    )

    private fun generateContentResponse(planJson: String): String =
        """
        {
          "candidates": [
            {
              "content": {
                "parts": [
                  {
                    "text": ${planJson.quoteForJson()}
                  }
                ]
              }
            }
          ],
          "usageMetadata": {
            "promptTokenCount": 100,
            "candidatesTokenCount": 300
          }
        }
        """.trimIndent()

    private fun validPlanJson(): String {
        val days = (1..7).joinToString(separator = ",") { day ->
            """
            {
              "day": $day,
              "title": "Day $day",
              "durationMinutes": 35,
              "exercises": [
                {"name": "Squat", "sets": 3, "reps": "10", "estimatedDurationMinutes": 8}
              ]
            }
            """.trimIndent()
        }
        return """{"days":[$days]}"""
    }

    private fun duplicateDayPlanJson(): String {
        val days = (1..7).joinToString(separator = ",") {
            """
            {
              "day": 1,
              "title": "Duplicate",
              "durationMinutes": 35,
              "exercises": [
                {"name": "Squat", "sets": 3, "reps": "10", "estimatedDurationMinutes": 8}
              ]
            }
            """.trimIndent()
        }
        return """{"days":[$days]}"""
    }

    private fun String.quoteForJson(): String =
        "\"" + replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
}

private class FakeGeminiApiService(
    private val results: List<Any>
) : GeminiApiService {
    private var index = 0

    override suspend fun generatePlan(
        request: GeminiGenerateContentRequest,
        apiKey: String,
        configuration: GeminiBenchmarkConfiguration
    ): GeminiApiCallResult {
        val result = results[index++]
        if (result is Throwable) throw result
        return result as GeminiApiCallResult
    }
}

private class SequenceBenchmarkClock(
    vararg values: Long
) : BenchmarkClock {
    private val queue = values.toMutableList()

    override fun nowMillis(): Long = queue.removeAt(0)
}
