# Story SETUP-005: Technical Spike - Gemini API 5-Second Latency

Status: done

Completion Note: Ultimate context engine analysis completed - comprehensive developer guide created.

## Story

As the FitLife developer,
I need to validate whether Gemini API workout-plan generation can reliably complete within 5 seconds on the intended free-tier setup,
so that the MVP can decide whether AI plan generation remains primary in v1.0 or static fallback templates become the primary source.

## Acceptance Criteria

1. A temporary Gemini latency benchmark harness sends representative workout-plan prompts to the Gemini API using the current project architecture boundary, not UI or ViewModel code.
2. The harness records at least 10 timed calls against the selected Gemini model and captures per-call success/failure, total latency, network/request latency where measurable, parsing latency, HTTP status or error category, retry usage, response token/size metadata where available, and whether returned JSON maps to the expected workout-plan shape.
3. Pass decision: average successful end-to-end generation latency is less than 5 seconds, and success rate is at least 95%. For the minimum 10-call run, this means all 10 calls must succeed; if 20 calls are run, at least 19 must succeed.
4. Fail decision: if pass criteria are not met, the report recommends making static fallback templates the primary source and using Gemini only as an enhancement, matching the PRD and architecture contingency.
5. The benchmark proves timeout and fallback behavior: calls exceeding 5 seconds are treated as failures, do not block the caller, and route to a local fallback plan path in the harness.
6. The spike does not implement production onboarding, production workout generation UI, Room/Firestore persistence, dynamic equipment rerouting, personalization beyond test profiles, or app navigation.
7. `./gradlew.bat test --no-daemon --console=plain` passes, and any module touched by the harness compiles.

## Tasks / Subtasks

- [x] Confirm benchmark scope, model, and environment. (AC: 1, 2, 3)
  - [x] Select the model to test and record why it was chosen. Prefer the fastest current generally available Gemini model suitable for JSON text output unless the architecture/product owner specifies another model.
  - [x] Record device/network context: device model, Android API, connection type, approximate location/region, time of run, battery/thermal state if relevant, and whether the app is debug or release.
  - [x] Verify the active free-tier/project quota in Google AI Studio before running; do not assume published limits are guaranteed.
  - [x] Use representative FitLife test profiles covering beginner/intermediate, goal, location/equipment constraints, and 7-day plan output.
- [x] Add only the dependencies needed for the spike. (AC: 1, 6, 7)
  - [x] Add Retrofit/OkHttp/Gson or kotlinx serialization aliases through `gradle/libs.versions.toml` only if they are not already present and are required for the harness.
  - [x] Scope network dependencies to `:feature:workout:workout-data`; keep `:feature:workout:workout-domain` free of Retrofit, OkHttp, Gson DTOs, Android framework types, and Gemini SDK/request classes.
  - [x] Do not add a broad Google AI SDK unless it is intentionally selected after checking Android compatibility and architecture fit; the architecture currently names Retrofit service integration.
- [x] Build a temporary Gemini latency benchmark harness. (AC: 1, 2, 5, 6)
  - [x] Implement or prototype `GeminiApiService.generatePlan(request)` behind the data layer boundary.
  - [x] Keep the API key out of source code and reports. Read it from local configuration, environment, or BuildConfig generated from uncommitted local properties.
  - [x] Use `withTimeout(5000)` or an equivalent coroutine timeout around the end-to-end API call and parsing path.
  - [x] Use bounded retry/backoff only when measuring retry behavior; report both first-attempt latency and final latency separately so retries do not hide slow primary responses.
  - [x] Request structured JSON output and validate the response maps to the expected domain shape before counting a call as successful.
  - [x] Keep the harness behind a debug-only, test-only, or command-style entry point so it cannot become accidental production UX.
- [x] Capture metrics and produce a decision report. (AC: 2, 3, 4, 5)
  - [x] Record p50, p95, min, max, average latency, success rate, timeout count, rate-limit count, parse-error count, and fallback count.
  - [x] Record prompt size, output size/token estimate, selected model, endpoint/API version, request schema version, and temperature/max-output settings.
  - [x] Save the decision report in `_bmad-output/implementation-artifacts/spike-gemini-api-5-s-latency-report.md`.
  - [x] Include a clear pass/fail recommendation and follow-up guidance for `wp-001-gemini-api-service-prompt-builder` and `wp-002-generateworkoutplan-use-case-with-fallback-asset`.
- [x] Verify and clean scope. (AC: 6, 7)
  - [x] Run focused compile/test tasks for touched modules.
  - [x] Run `./gradlew.bat test --no-daemon --console=plain`.
  - [x] Ensure automated tests use fakes or MockWebServer and never require a real Gemini API key, live network, Firebase, or paid quota.
  - [x] Ensure no API keys, raw user health profiles, full prompts with sensitive data, or Gemini responses containing personal data are logged or committed.

### Review Findings

- [x] [Review][Patch] Classify non-2xx Gemini responses before parsing [feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiLatencyBenchmarkRunner.kt:56] - AC2 requires HTTP status or error category and rate-limit/HTTP-error metrics. `HttpGeminiApiService` returns non-2xx responses as normal call results, and the runner parses the error body as a workout plan, so 429/401/5xx responses can be reported as `ParseError` with `rateLimitCount` and `httpErrorCount` still zero.
- [x] [Review][Patch] Apply the 5-second timeout to end-to-end request plus parsing [feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiLatencyBenchmarkRunner.kt:52] - AC5 requires calls exceeding 5 seconds to be treated as failures. The current `withTimeout` wraps only `apiService.generatePlan`; parsing happens after the timeout block, so a response that arrives just under 5 seconds can parse past 5 seconds and still be recorded as success.
- [x] [Review][Patch] Exercise or explicitly record the local fallback plan path [feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiLatencyBenchmarkRunner.kt:93] - AC5 says timeout failures route to a local fallback plan path. The current timeout path only sets `fallbackUsed = true`; it does not load, identify, or validate a local fallback path, so the report overclaims fallback behavior.
- [x] [Review][Patch] Include retry count and error category in the per-call report [feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiLiveLatencyBenchmarkManualTest.kt:188] - AC2 requires retry usage and HTTP status or error category per call. Samples carry `retryCount` and `errorCategory`, but the report table omits both fields.
- [x] [Review][Patch] Render the generated Markdown report without leading indentation [feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiLiveLatencyBenchmarkManualTest.kt:122] - The generated report begins with leading spaces, causing Markdown headings and tables to render as code blocks rather than a readable decision report.
- [x] [Review][Patch] Require unique day numbers 1 through 7 when validating Gemini plans [feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiPlanResponseParser.kt:55] - The parser accepts seven days where every `day` value is `1`; it should require the set of day numbers to equal `1..7`.
- [x] [Review][Patch] Rethrow coroutine cancellation instead of recording it as HTTP error [feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiLatencyBenchmarkRunner.kt:82] - The broad `Exception` catch can swallow cancellation paths and misreport them as `HttpError`; `CancellationException` should be rethrown before the broad catch.
- [x] [Review][Patch] Avoid reporting zero successful latency as `0 ms` when there are no successful calls [feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/gemini/GeminiBenchmarkSummarizer.kt:18] - The current fail report shows average/p50/p95 successful latency as `0 ms` even though there were zero successful calls. Use `N/A` or nullable values in the report to avoid misleading evidence.
- [x] [Review][Patch] Use monotonic time for benchmark latency measurements [feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/BenchmarkClock.kt:8] - `System.currentTimeMillis()` can move backward or forward during a run; elapsed benchmark measurements should use a monotonic source such as `System.nanoTime()`.

## Dev Notes

### Current State

- SETUP-001 through SETUP-004 are complete. The project already has the multi-module graph, shared domain/data/UI foundations, Hilt app setup, Firebase Analytics/Crashlytics, and a completed ML Kit performance spike.
- `:feature:workout:workout-domain` and `:feature:workout:workout-data` exist and are the natural boundary for Gemini workout-plan integration. `:feature:workout:workout-ui` should not be touched for this setup spike unless a temporary debug-only trigger is unavoidable.
- `gradle/libs.versions.toml` currently includes AGP `9.2.1`, Kotlin `2.2.10`, Compose BOM `2026.02.01`, Hilt `2.59.2`, KSP `2.3.4`, coroutines `1.11.0`, Firebase BoM `34.14.0`, CameraX `1.6.1`, and ML Kit Pose Detection `18.0.0-beta5`. Do not upgrade existing versions opportunistically.
- Retrofit, OkHttp, Gson, Room, and Firestore are planned by architecture but are not currently present in the version catalog. Add only what this spike needs.
- `core/core-data` already has `SafeCall`, `ResponseToResult`, connectivity utilities, `AnalyticsLogger`, and `CrashReporter`. Reuse or extend these patterns where they fit; do not create duplicate result/error infrastructure.

### Existing Files To Read Before Editing

- `gradle/libs.versions.toml`: all new dependency aliases belong here.
- `settings.gradle.kts`: verify module names; do not add new modules for this spike unless absolutely necessary.
- `feature/workout/workout-domain/build.gradle.kts`: keep domain platform-independent.
- `feature/workout/workout-data/build.gradle.kts`: likely location for Retrofit/OkHttp/Gemini data dependencies and benchmark implementation.
- `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/Result.kt` and `NetworkErrors.kt`: reuse existing result/error contracts.
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/repository/SafeCall.kt`: inspect before creating network error handling.
- `_bmad-output/implementation-artifacts/setup-004-technical-spike-ml-kit-pose-detection-15-fps.md`: previous spike structure, report expectations, and scope discipline.

### Architecture Compliance

- This story is a Week 1 technical spike, not the production workout-plan feature. The architecture pass criteria are explicit: confirm less than 5-second average plan generation over 10 calls with at least 95% success; fail action is static fallback templates as primary source.
- Follow the architecture flow for later production code: prepare prompt, call Gemini through `GeminiApiService.generatePlan(request)`, apply 5-second timeout, parse JSON response into domain `WorkoutPlan`, cache before API call in later stories, and fallback to local JSON on API failure or latency over 5 seconds.
- Keep Clean Architecture boundaries intact:
  - Domain may define pure Kotlin test-profile, benchmark-metric, decision, and workout-plan shape models if useful.
  - Data owns Retrofit/OkHttp DTOs, API request/response classes, API-key injection, HTTP client setup, and parsing.
  - UI should not call Gemini directly and should not own benchmark logic.
  - App should not absorb workout feature implementation.
- Do not implement `wp-001-gemini-api-service-prompt-builder`, `wp-002-generateworkoutplan-use-case-with-fallback-asset`, `wp-003-workoutplan-room-entities-daos`, home screen plan states, or session rerouting in this setup story.
- Do not introduce MVVM. If a temporary debug UI becomes necessary, use the existing MVI vocabulary and keep it debug-only.
- Preserve package root `com.aml_sakr.fitlife`, `minSdk = 30`, `targetSdk = 36`, and Java 11 compatibility.

### Library and Framework Requirements

- Gemini `models.generateContent` is the relevant API method for generating a model response. The REST endpoint is `POST https://generativelanguage.googleapis.com/v1beta/{model=models/*}:generateContent`, and the request requires `contents`. Source: https://ai.google.dev/api/generate-content
- Gemini generation config supports JSON output via `responseMimeType = "application/json"` with a compatible response schema. Use the current official request shape from the docs at implementation time because the docs show both API-reference and structured-output examples. Sources: https://ai.google.dev/api/generate-content and https://ai.google.dev/gemini-api/docs/structured-output
- Gemini rate limits are measured across requests per minute, input tokens per minute, and requests per day; limits apply per project rather than per API key, and active limits should be checked in AI Studio because they can vary by model and account tier. Source: https://ai.google.dev/gemini-api/docs/rate-limits
- The benchmark should prefer a low-latency text generation model that supports structured JSON output. Record the exact model name, e.g. `models/gemini-3.5-flash` or the current fastest stable Flash/Flash-Lite candidate, instead of using an undocumented shorthand.
- If Retrofit is added, prefer a stable Maven Central release and pin it in `gradle/libs.versions.toml`. Maven Central currently lists Retrofit metadata under `com.squareup.retrofit2:retrofit`; verify the latest stable version before adding it. Source: https://repo1.maven.org/maven2/com/squareup/retrofit2/retrofit/
- If OkHttp is added directly, prefer a stable Maven Central release and pin it in the version catalog. Source: https://central.sonatype.com/artifact/com.squareup.okhttp3/okhttp/versions

### Benchmark Design Guidance

- Required metrics:
  - total benchmark duration
  - per-call end-to-end latency from request start through parsed domain object
  - first-attempt latency and final latency if retries are enabled
  - p50, p95, min, max, and average latency
  - success, timeout, HTTP error, rate-limit, network error, parse error, and fallback counts
  - selected model, endpoint/API version, generation config, prompt size, response size/token estimate
  - JSON schema validation result and domain-mapping result
- Required environment notes:
  - device model and Android API level if run on device
  - network type and rough quality notes
  - run timestamp and region/location context
  - whether calls used free tier, paid tier, emulator, local JVM, or Android instrumentation
  - quota/rate-limit observations before and during the run
- Pass/fail interpretation:
  - Pass only if successful end-to-end calls average under 5 seconds and success rate meets the acceptance criteria.
  - A single quick response is not enough; use the complete run.
  - A call that returns quickly but cannot be parsed into the expected plan shape is a failure.
  - A call that needs fallback because Gemini exceeded 5 seconds is a timeout failure for the spike, even if fallback returns a usable plan.
  - If no API key or quota is available, report `blocked` or `inconclusive`; do not fake a pass with mocked responses.

### Regression and Scope Guardrails

- Do not hardcode, commit, print, or include the Gemini API key in reports, logs, build files, source code, screenshots, or test fixtures.
- Do not log sensitive health/profile data. Use synthetic test profiles and redact prompt/response details in the report if they could resemble user data.
- Do not run live Gemini calls from automated unit tests or CI. Real API calls are manual spike verification only.
- Do not add production navigation, onboarding screens, workout-plan UI states, Room entities, Firestore sync, or cache-first production behavior in this setup story.
- Do not let Retrofit DTOs, Gemini request classes, JSON adapters, API keys, or Android framework types leak into domain APIs.
- Do not mask Gemini latency by measuring only client-side fallback time. The report must separate Gemini response latency from fallback latency.
- Do not use unlimited retries; retries can exhaust free-tier quotas and distort the 5-second decision.

### Testing Requirements

- Required automated command: `./gradlew.bat test --no-daemon --console=plain`.
- Also run targeted compile/test tasks for touched modules, for example:
  - `./gradlew.bat :feature:workout:workout-domain:test --no-daemon --console=plain` if domain metric or decision logic is added.
  - `./gradlew.bat :feature:workout:workout-data:compileDebugKotlin --no-daemon --console=plain` if data/network code is touched.
- Unit tests should cover pure metric aggregation, pass/fail/inconclusive decision logic, timeout classification, fallback classification, and JSON/domain mapping with local fixtures.
- Network boundary tests should use fakes or MockWebServer if added. They must not require a real API key, live Gemini API, Firebase, or internet.
- Manual live Gemini verification is required for AC 1-5 and should be summarized in the spike report.

### Previous Story Intelligence

- SETUP-004 established a useful spike pattern: domain-only metric models and decision logic, data-layer implementation details, instrumentation/manual evidence where required, a dedicated report under `_bmad-output/implementation-artifacts/`, and no accidental production navigation.
- SETUP-004 initially had inconclusive runs until the benchmark environment was corrected. Apply the same discipline here: if quota, API key, network, or model selection makes the run invalid, report inconclusive or blocked rather than forcing a pass/fail.
- SETUP-003 established `AnalyticsLogger` and `CrashReporter` in `:core:core-data` without Firebase SDK types in public API. Preserve that boundary if diagnostics are added.
- SETUP-002 established `Result`, `NetworkErrors`, `SafeCall`, preferences, connectivity, MVI contracts, and shared theme in core modules. Reuse existing contracts where useful; do not add duplicate result or MVI packages.
- SETUP-001 preserved `MainActivity` as the single Compose host. Do not wire a permanent workout route from this spike.
- Previous stories consistently centralized versions in `gradle/libs.versions.toml`, kept generated build artifacts out of commits, and verified with `test`, `lint`, and module compile/test tasks where relevant.

### Git Intelligence

- Recent commits:
  - `5a31f9f Merge pull request #3 from Amlsakr/feature/setup-004`
  - `642563e code review SETUP-004`
  - `dfe3378 implement SETUP-004`
  - `693c9b6 implement SETUP-004`
  - `eca9f67 Merge pull request #2 from Amlsakr/feature/setup-003-firebase-crashlytics`
- Recent work has been setup/spike-focused and strongly scoped. Continue that pattern by producing benchmark code and a decision report, not by beginning full workout-plan implementation.

### References

- Sprint status: `_bmad-output/implementation-artifacts/sprint-status.yaml`
- Epics: `_bmad-output/planning-artifacts/epics.md`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md`
- Project context: `_bmad-output/project-context.md`
- Previous story: `_bmad-output/implementation-artifacts/setup-004-technical-spike-ml-kit-pose-detection-15-fps.md`
- Version catalog: `gradle/libs.versions.toml`
- Workout data build file: `feature/workout/workout-data/build.gradle.kts`
- Workout domain build file: `feature/workout/workout-domain/build.gradle.kts`
- Gemini GenerateContent API: https://ai.google.dev/api/generate-content
- Gemini structured output docs: https://ai.google.dev/gemini-api/docs/structured-output
- Gemini rate limit docs: https://ai.google.dev/gemini-api/docs/rate-limits
- Retrofit Maven metadata: https://repo1.maven.org/maven2/com/squareup/retrofit2/retrofit/
- OkHttp Maven Central: https://central.sonatype.com/artifact/com.squareup.okhttp3/okhttp/versions

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-06-07: Started SETUP-005 dev-story workflow, loaded project context and story context, and moved sprint/story status to `in-progress`.
- 2026-06-07: Added failing domain tests for Gemini benchmark summarization, pass/fail/inconclusive decision logic, and representative 10-call profile generation.
- 2026-06-07: Implemented domain benchmark models, representative profiles, summary calculation, and decision maker in `:feature:workout:workout-domain`; focused domain tests passed.
- 2026-06-07: Added failing data-layer tests for request building, response parsing, timeout fallback classification, and fake-service benchmark execution.
- 2026-06-07: Added Gson dependency through `gradle/libs.versions.toml`; no Retrofit/OkHttp or Google AI SDK dependency was added.
- 2026-06-07: Implemented `GeminiApiService`, `HttpGeminiApiService`, `GeminiWorkoutPromptBuilder`, `GeminiPlanResponseParser`, `GeminiLatencyBenchmarkRunner`, and benchmark clock abstraction in `:feature:workout:workout-data`.
- 2026-06-07: Added a manual live benchmark test entry point gated by `FITLIFE_RUN_GEMINI_LIVE_BENCHMARK=true`, reading `GEMINI_API_KEY` from `local.properties` or environment and writing the spike report.
- 2026-06-07: Initial live test skipped because the test JVM did not resolve `local.properties` from project root; fixed by passing `FITLIFE_PROJECT_ROOT` to unit test tasks.
- 2026-06-07: Live benchmark ran 10 calls against `models/gemini-2.5-flash-lite` with 5-second timeout and verified quota flag. Result: fail decision, 0/10 successful calls, 10/10 timeout fallback events.
- 2026-06-07: Validation passed: `./gradlew.bat :feature:workout:workout-domain:test :feature:workout:workout-data:testDebugUnitTest --no-daemon --console=plain` and `./gradlew.bat test --no-daemon --console=plain`.

### Completion Notes List

- Implemented a scoped Gemini latency benchmark harness without production UI, navigation, Room/Firestore persistence, or production workout-plan generation.
- Added domain-only benchmark models and decision logic so pass, fail, and inconclusive outcomes are explicit and testable.
- Added representative beginner/intermediate synthetic profiles and a 10-call profile set matching the spike requirement.
- Added data-layer request building for structured JSON workout-plan output and response parsing/validation into a 7-day workout-plan draft shape.
- Added a small `HttpGeminiApiService` using `HttpURLConnection` to avoid introducing Retrofit/OkHttp before production integration stories need them.
- Added a manual live benchmark test runner that is skipped during normal tests unless `FITLIFE_RUN_GEMINI_LIVE_BENCHMARK=true` is set.
- Ran the live Gemini benchmark with `GEMINI_API_KEY` from local configuration. All 10 calls exceeded the 5-second timeout, so the report recommends making static fallback templates primary and using Gemini only as an enhancement.
- Full regression tests passed.
- Resolved all 9 code-review patch findings: HTTP/rate-limit classification, end-to-end timeout treatment, fallback path evidence, report retry/error columns, Markdown report formatting, duplicate-day validation, cancellation handling, no-success latency display, and monotonic timing.
- Re-ran the live Gemini benchmark after review fixes; SETUP-005 still fails the 5-second threshold with 10 timeout fallback events, now reported with clean Markdown tables and fallback path evidence.

### File List

- `_bmad-output/implementation-artifacts/setup-005-technical-spike-gemini-api-5-s-latency.md`
- `_bmad-output/implementation-artifacts/spike-gemini-api-5-s-latency-report.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `gradle/libs.versions.toml`
- `feature/workout/workout-data/build.gradle.kts`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/BenchmarkClock.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiApiService.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiFallbackPlanProvider.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiGenerateContentModels.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiLatencyBenchmarkRunner.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiPlanResponseParser.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiWorkoutPromptBuilder.kt`
- `feature/workout/workout-data/src/main/java/com/aml_sakr/fitlife/feature/workout/data/gemini/HttpGeminiApiService.kt`
- `feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiLatencyBenchmarkRunnerTest.kt`
- `feature/workout/workout-data/src/test/java/com/aml_sakr/fitlife/feature/workout/data/gemini/GeminiLiveLatencyBenchmarkManualTest.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/gemini/GeminiBenchmarkDecisionMaker.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/gemini/GeminiBenchmarkModels.kt`
- `feature/workout/workout-domain/src/main/java/com/aml_sakr/fitlife/feature/workout/domain/gemini/GeminiBenchmarkSummarizer.kt`
- `feature/workout/workout-domain/src/test/java/com/aml_sakr/fitlife/feature/workout/domain/gemini/GeminiBenchmarkMetricsTest.kt`

### Change Log

- 2026-06-07: Implemented SETUP-005 Gemini latency spike harness, live manual benchmark runner, automated guardrail tests, and spike report. Live result failed the 5-second threshold with 10 timeout fallback events; story moved to review.
- 2026-06-08: Applied all code review patches, regenerated the live spike report, reran focused and full tests, and moved story to done.
