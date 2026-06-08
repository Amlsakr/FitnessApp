package com.aml_sakr.fitlife.feature.workout.data.gemini

import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkConfiguration
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkDecision
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkDecisionMaker
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkEnvironment
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkExecutionTarget
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkOutcome
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkProfiles
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkRun
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkSummary
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkSummarizer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Path
import java.time.OffsetDateTime
import java.util.Properties

class GeminiLiveLatencyBenchmarkManualTest {

    @Test
    fun `manual live Gemini benchmark writes spike report`() = runTest {
        assumeTrue(
            "Set FITLIFE_RUN_GEMINI_LIVE_BENCHMARK=true to run live Gemini calls.",
            liveSetting("FITLIFE_RUN_GEMINI_LIVE_BENCHMARK") == "true"
        )

        val apiKey = loadGeminiApiKey()
        assumeTrue("GEMINI_API_KEY must be present in local.properties or environment.", apiKey.isNotBlank())

        val configuration = GeminiBenchmarkConfiguration(
            modelName = liveSetting("FITLIFE_GEMINI_MODEL") ?: "models/gemini-2.5-flash-lite",
            apiVersion = "v1beta",
            endpoint = "v1beta/models/${(liveSetting("FITLIFE_GEMINI_MODEL") ?: "models/gemini-2.5-flash-lite").removePrefix("models/")}:generateContent",
            responseMimeType = "application/json",
            temperature = 0.2,
            maxOutputTokens = 4096,
            timeoutMillis = 5_000L,
            maxRetries = 0
        )
        val environment = GeminiBenchmarkEnvironment(
            deviceModel = InetAddress.getLocalHost().hostName,
            androidApiLevel = null,
            networkType = "developer machine network",
            approximateRegion = "Egypt/Cairo local development environment",
            runTimestamp = OffsetDateTime.now().toString(),
            executionTarget = GeminiBenchmarkExecutionTarget.LocalJvm,
            quotaVerified = liveSetting("FITLIFE_GEMINI_QUOTA_VERIFIED") == "true",
            usedLiveGeminiApi = true
        )
        val runner = GeminiLatencyBenchmarkRunner(
            apiService = HttpGeminiApiService(),
            promptBuilder = GeminiWorkoutPromptBuilder(),
            responseParser = GeminiPlanResponseParser()
        )

        val run = runner.run(
            apiKey = apiKey,
            environment = environment,
            configuration = configuration,
            profiles = GeminiBenchmarkProfiles.minimumTenCallProfiles()
        )
        val summary = GeminiBenchmarkSummarizer.summarize(run)
        val decision = GeminiBenchmarkDecisionMaker.decide(summary)
        GeminiLatencyReportWriter.write(
            path = projectRoot().resolve("_bmad-output").resolve("implementation-artifacts")
                .resolve("spike-gemini-api-5-s-latency-report.md"),
            run = run,
            summary = summary,
            decision = decision
        )

        assertTrue("Expected 10 live Gemini calls to be recorded.", summary.totalCallCount == 10)
        assertTrue(
            "Live benchmark must produce a pass or fail decision, not an inconclusive result.",
            decision.outcome == GeminiBenchmarkOutcome.Pass || decision.outcome == GeminiBenchmarkOutcome.Fail
        )
    }

    private fun loadGeminiApiKey(): String {
        System.getenv("GEMINI_API_KEY")?.takeIf { it.isNotBlank() }?.let { return it }
        val localProperties = projectRoot().resolve("local.properties")
        if (!Files.exists(localProperties)) return ""
        return Files.newInputStream(localProperties).use { input ->
            Properties().apply { load(input) }
        }.getProperty("GEMINI_API_KEY", "")
    }

    private fun liveSetting(name: String): String? =
        System.getProperty(name) ?: System.getenv(name)

    private fun projectRoot(): Path =
        Path.of(System.getProperty("FITLIFE_PROJECT_ROOT") ?: ".")
}

private object GeminiLatencyReportWriter {
    fun write(
        path: Path,
        run: GeminiBenchmarkRun,
        summary: GeminiBenchmarkSummary,
        decision: GeminiBenchmarkDecision
    ) {
        Files.createDirectories(path.parent)
        Files.writeString(path, render(run, summary, decision))
    }

    private fun render(
        run: GeminiBenchmarkRun,
        summary: GeminiBenchmarkSummary,
        decision: GeminiBenchmarkDecision
    ): String {
        val status = when (decision.outcome) {
            GeminiBenchmarkOutcome.Pass -> "pass"
            GeminiBenchmarkOutcome.Fail -> "fail"
            GeminiBenchmarkOutcome.Inconclusive -> "inconclusive"
        }
        val latencyMetrics = if (summary.successfulCallCount == 0) {
            listOf(
                "| Average successful latency | N/A |",
                "| p50 successful latency | N/A |",
                "| p95 successful latency | N/A |",
                "| Min successful latency | N/A |",
                "| Max successful latency | N/A |"
            )
        } else {
            listOf(
                "| Average successful latency | ${"%.0f".format(summary.averageSuccessfulLatencyMillis)} ms |",
                "| p50 successful latency | ${summary.p50SuccessfulLatencyMillis} ms |",
                "| p95 successful latency | ${summary.p95SuccessfulLatencyMillis} ms |",
                "| Min successful latency | ${summary.minSuccessfulLatencyMillis} ms |",
                "| Max successful latency | ${summary.maxSuccessfulLatencyMillis} ms |"
            )
        }.joinToString(separator = "\n")
        val reasons = decision.reasons.joinToString(separator = "\n") { "- $it" }
        val sampleRows = run.samples.joinToString(separator = "\n") { sample ->
            "| ${sample.callIndex} | ${sample.status} | ${sample.totalLatencyMillis} ms | ${sample.requestLatencyMillis ?: 0} ms | ${sample.parsingLatencyMillis} ms | ${sample.httpStatusCode ?: ""} | ${sample.retryCount} | ${sample.errorCategory ?: ""} | ${sample.promptSizeChars} | ${sample.responseSizeChars} | ${sample.outputTokenEstimate ?: ""} | ${sample.schemaValid} | ${sample.mappedToWorkoutPlan} | ${sample.fallbackUsed} | ${sample.fallbackPlanPath ?: ""} |"
        }

        return """
># Gemini API 5-Second Latency Spike Report
>
>Status: $status
>
>Date: ${run.environment.runTimestamp}
>
>Story: SETUP-005 - Technical Spike: Gemini API 5-Second Latency
>
>## Decision
>
>${decision.recommendation}
>
>Outcome: ${decision.outcome}
>
>Acceptance criteria satisfied: ${decision.satisfiesAcceptanceCriteria}
>
>Reasons:
>$reasons
>
>## Environment
>
>| Field | Value |
>| --- | --- |
>| Execution target | ${run.environment.executionTarget} |
>| Device / host | ${run.environment.deviceModel} |
>| Android API level | ${run.environment.androidApiLevel ?: "not applicable"} |
>| Network | ${run.environment.networkType} |
>| Region | ${run.environment.approximateRegion} |
>| Quota verified before run | ${run.environment.quotaVerified} |
>| Live Gemini API used | ${run.environment.usedLiveGeminiApi} |
>
>## Configuration
>
>| Field | Value |
>| --- | --- |
>| Model | ${run.configuration.modelName} |
>| Endpoint | ${run.configuration.endpoint} |
>| API version | ${run.configuration.apiVersion} |
>| Response MIME type | ${run.configuration.responseMimeType} |
>| Temperature | ${run.configuration.temperature} |
>| Max output tokens | ${run.configuration.maxOutputTokens} |
>| Timeout | ${run.configuration.timeoutMillis} ms |
>| Max retries | ${run.configuration.maxRetries} |
>
>## Metrics
>
>| Metric | Value |
>| --- | --- |
>| Total calls | ${summary.totalCallCount} |
>| Successful calls | ${summary.successfulCallCount} |
>| Success rate | ${"%.2f".format(summary.successRate * 100)}% |
>$latencyMetrics
>| Timeouts | ${summary.timeoutCount} |
>| Rate limits | ${summary.rateLimitCount} |
>| HTTP errors | ${summary.httpErrorCount} |
>| Network errors | ${summary.networkErrorCount} |
>| Parse errors | ${summary.parseErrorCount} |
>| Fallback count | ${summary.fallbackCount} |
>
>## Per-Call Results
>
>| Call | Status | Total latency | Request latency | Parsing latency | HTTP | Retries | Error category | Prompt chars | Response chars | Output tokens | Schema valid | Mapped | Fallback | Fallback path |
>| --- | --- | ---: | ---: | ---: | --- | ---: | --- | ---: | ---: | ---: | --- | --- | --- | --- |
>$sampleRows
>
>## Follow-Up For Implementation Stories
>
>- `wp-001-gemini-api-service-prompt-builder` can reuse the structured prompt/schema shape but should convert this spike code into production repository boundaries deliberately.
>- `wp-002-generateworkoutplan-use-case-with-fallback-asset` should keep the 5-second timeout and local fallback path behavior proven by tests here.
>- If this run fails, static fallback templates should become the primary v1.0 plan source and Gemini should become an enhancement.
        """.trimMargin(marginPrefix = ">")
    }
}
