package com.aml_sakr.fitlife.feature.workout.data.gemini

import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkConfiguration
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkEnvironment
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkRun
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkSample
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiCallStatus
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiWorkoutProfile
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.SocketTimeoutException

class GeminiLatencyBenchmarkRunner(
    private val apiService: GeminiApiService,
    private val promptBuilder: GeminiWorkoutPromptBuilder,
    private val responseParser: GeminiPlanResponseParser,
    private val clock: BenchmarkClock = SystemBenchmarkClock
) {
    suspend fun run(
        apiKey: String,
        environment: GeminiBenchmarkEnvironment,
        configuration: GeminiBenchmarkConfiguration,
        profiles: List<GeminiWorkoutProfile>
    ): GeminiBenchmarkRun {
        val samples = profiles.mapIndexed { index, profile ->
            runSingleCall(
                callIndex = index + 1,
                profile = profile,
                apiKey = apiKey,
                configuration = configuration
            )
        }
        return GeminiBenchmarkRun(
            environment = environment,
            configuration = configuration,
            samples = samples
        )
    }

    private suspend fun runSingleCall(
        callIndex: Int,
        profile: GeminiWorkoutProfile,
        apiKey: String,
        configuration: GeminiBenchmarkConfiguration
    ): GeminiBenchmarkSample {
        val request = promptBuilder.buildRequest(profile, configuration)
        val promptSizeChars = request.contents.sumOf { content -> content.parts.sumOf { it.text.length } }
        val startMillis = clock.nowMillis()

        return try {
            val callResult = withTimeout(configuration.timeoutMillis) {
                apiService.generatePlan(request, apiKey, configuration)
            }
            val responseReceivedMillis = clock.nowMillis()
            val parsed = responseParser.parse(callResult.responseBody)
            val parsedMillis = clock.nowMillis()
            val status = if (parsed.isValidPlan) GeminiCallStatus.Success else GeminiCallStatus.ParseError
            GeminiBenchmarkSample(
                callIndex = callIndex,
                status = status,
                firstAttemptLatencyMillis = parsedMillis - startMillis,
                totalLatencyMillis = parsedMillis - startMillis,
                requestLatencyMillis = responseReceivedMillis - startMillis,
                parsingLatencyMillis = parsedMillis - responseReceivedMillis,
                httpStatusCode = callResult.httpStatusCode,
                retryCount = 0,
                responseSizeChars = callResult.responseSizeChars,
                promptSizeChars = promptSizeChars,
                outputTokenEstimate = parsed.candidatesTokenCount,
                schemaValid = parsed.isValidPlan,
                mappedToWorkoutPlan = parsed.plan != null,
                fallbackUsed = status != GeminiCallStatus.Success,
                errorCategory = if (status == GeminiCallStatus.Success) null else "parse_error"
            )
        } catch (_: TimeoutCancellationException) {
            timeoutSample(callIndex, startMillis, promptSizeChars, configuration.timeoutMillis)
        } catch (_: SocketTimeoutException) {
            timeoutSample(callIndex, startMillis, promptSizeChars, configuration.timeoutMillis)
        } catch (_: IOException) {
            errorSample(callIndex, startMillis, promptSizeChars, GeminiCallStatus.NetworkError, "network_error")
        } catch (_: Exception) {
            errorSample(callIndex, startMillis, promptSizeChars, GeminiCallStatus.HttpError, "http_or_unknown_error")
        }
    }

    private fun timeoutSample(
        callIndex: Int,
        startMillis: Long,
        promptSizeChars: Int,
        timeoutMillis: Long
    ): GeminiBenchmarkSample {
        val endMillis = clock.nowMillis().coerceAtLeast(startMillis + timeoutMillis)
        return GeminiBenchmarkSample(
            callIndex = callIndex,
            status = GeminiCallStatus.Timeout,
            firstAttemptLatencyMillis = endMillis - startMillis,
            totalLatencyMillis = endMillis - startMillis,
            requestLatencyMillis = endMillis - startMillis,
            parsingLatencyMillis = 0L,
            httpStatusCode = null,
            retryCount = 0,
            responseSizeChars = 0,
            promptSizeChars = promptSizeChars,
            outputTokenEstimate = null,
            schemaValid = false,
            mappedToWorkoutPlan = false,
            fallbackUsed = true,
            errorCategory = "timeout"
        )
    }

    private fun errorSample(
        callIndex: Int,
        startMillis: Long,
        promptSizeChars: Int,
        status: GeminiCallStatus,
        category: String
    ): GeminiBenchmarkSample {
        val endMillis = clock.nowMillis()
        return GeminiBenchmarkSample(
            callIndex = callIndex,
            status = status,
            firstAttemptLatencyMillis = endMillis - startMillis,
            totalLatencyMillis = endMillis - startMillis,
            requestLatencyMillis = endMillis - startMillis,
            parsingLatencyMillis = 0L,
            httpStatusCode = null,
            retryCount = 0,
            responseSizeChars = 0,
            promptSizeChars = promptSizeChars,
            outputTokenEstimate = null,
            schemaValid = false,
            mappedToWorkoutPlan = false,
            fallbackUsed = true,
            errorCategory = category
        )
    }
}
