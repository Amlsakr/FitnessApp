# Story SETUP-003: Firebase & Crashlytics Integration

Status: done

Completion Note: Ultimate context engine analysis completed - comprehensive developer guide created.

## Story

As the FitLife developer,
I need Firebase services connected so that the app can capture crashes and log analytics events,
while leaving Auth, Firestore data models, and sync behavior for their later feature stories.

## Acceptance Criteria

1. Firebase is registered for the current Android application id `com.aml_sakr.fitlife`, and `app/google-services.json` is present in the app module root with the exact filename.
2. Gradle Firebase wiring is centralized in `gradle/libs.versions.toml`, uses pinned versions, applies the Google Services plugin and Crashlytics Gradle plugin only to `:app`, and does not hardcode dependency coordinates in module build files.
3. `:app` depends on `:core:core-data` so core Firebase analytics/crash-reporting bindings are available through Hilt, while existing `:app` Compose/Hilt setup remains intact.
4. Crashlytics is integrated for app crashes and non-fatal exception recording without adding debug-only crash buttons or intentionally crashing production paths.
5. Analytics events can be logged through a reusable core abstraction, with a Firebase-backed implementation in `:core:core-data` and no Firebase SDK types leaking into domain APIs or UI contracts.
6. `FitnessApplication` remains annotated with `@HiltAndroidApp`, remains registered in `AndroidManifest.xml`, and preserves app startup behavior.
7. No Firebase Auth, Firestore repositories, security rules, Room/Firestore sync, service account credentials, private keys, or feature-specific analytics events are implemented in this story.
8. `./gradlew.bat test` and a Firebase-relevant build verification such as `./gradlew.bat :app:assembleDebug` pass.

## Tasks / Subtasks

- [x] Add Firebase Gradle catalog entries. (AC: 2)
  - [x] Add pinned versions for Firebase BoM, Google Services plugin, and Crashlytics Gradle plugin in `gradle/libs.versions.toml`.
  - [x] Add library aliases for `firebase-bom`, `firebase-analytics`, and `firebase-crashlytics`.
  - [x] Add plugin aliases for `com.google.gms.google-services` and `com.google.firebase.crashlytics`.
  - [x] Use main Firebase modules, not deprecated `*-ktx` artifacts.
- [x] Wire app-level Firebase plugins and dependencies. (AC: 1, 2, 3, 4)
  - [x] Add the Firebase plugin aliases to the root `build.gradle.kts` with `apply false`.
  - [x] Apply Google Services and Crashlytics plugins in `app/build.gradle.kts`.
  - [x] Add `implementation(platform(libs.firebase.bom))`, `implementation(libs.firebase.analytics)`, and `implementation(libs.firebase.crashlytics)` to `:app`.
  - [x] Add `implementation(project(":core:core-data"))` to `:app` only if needed for the core analytics/crash-reporting Hilt bindings.
  - [x] Place `google-services.json` at `app/google-services.json`; do not place it in library modules.
- [x] Add core analytics/crash-reporting abstractions. (AC: 5, 7)
  - [x] Add a core-data API such as `AnalyticsLogger` with methods for event logging and optional parameters.
  - [x] Add a core-data API such as `CrashReporter` with methods for non-fatal exception recording and optional user/property metadata.
  - [x] Keep these abstractions Android/Firebase-free at their public contract boundary where practical; do not put Firebase SDK types in domain or UI APIs.
  - [x] Add Firebase-backed implementations in `:core:core-data`.
  - [x] Add Hilt bindings in a core-only module such as `FirebaseDataModule` or an extension of `CoreDataModule`.
- [x] Preserve application startup. (AC: 4, 6)
  - [x] Keep `FitnessApplication` annotated with `@HiltAndroidApp`.
  - [x] Keep `android:name=".FitnessApplication"` in `app/src/main/AndroidManifest.xml`.
  - [x] If code is added to `FitnessApplication.onCreate()`, call `super.onCreate()` first and keep initialization minimal.
  - [x] Rely on Firebase automatic initialization from `google-services.json` where valid; only call `FirebaseApp.initializeApp(this)` if the implementation proves it is required.
- [x] Add focused verification. (AC: 5, 8)
  - [x] Add JVM tests for analytics/crash-reporting wrappers using fakes or mocks, not live Firebase.
  - [x] Add or update tests so the public abstraction behavior is covered without requiring a real Firebase project.
  - [x] Run `./gradlew.bat test --no-daemon --console=plain`.
  - [x] Run `./gradlew.bat :app:assembleDebug --no-daemon --console=plain` to verify plugin/resource processing.

### Review Findings

- [x] [Review][Patch] Align Crashlytics Gradle plugin pin with story/latest Firebase table [gradle/libs.versions.toml:17]
- [x] [Review][Patch] Add tests for Firebase-backed observability wrappers, not only the in-memory fakes [core/core-data/src/test/java/com/aml_sakr/fitlife/core/data/observability/AnalyticsLoggerTest.kt:9]

## Dev Notes

### Current State

- SETUP-001 created the multi-module Gradle scaffold, Hilt application setup, and the app shell.
- SETUP-002 added core domain/data/UI foundations and completed successfully. The current app already uses `FitnessApplication` with `@HiltAndroidApp`, `MainActivity` as the single Compose host, and `FitnessAppTheme` from `:core:core-ui`.
- Current `settings.gradle.kts` includes `:app`, `:core:core-data`, `:core:core-domain`, `:core:core-ui`, and auth/onboarding/workout/session/progress feature modules. It does not include the planned widget module; do not solve widget scaffolding in this story.
- Current Gradle catalog versions include AGP `9.2.1`, Kotlin `2.2.10`, Compose BOM `2026.02.01`, Hilt `2.59.2`, KSP `2.3.4`, coroutines `1.11.0`, and DataStore `1.2.1`. Do not upgrade existing versions opportunistically.
- The working tree was clean before this story was created.

### Existing Files To Read Before Editing

- `settings.gradle.kts`: plugin repositories already include Google, Maven Central, and Gradle Plugin Portal.
- `build.gradle.kts`: root plugin aliases are declared with `apply false`.
- `gradle/libs.versions.toml`: all dependency and plugin additions must be centralized here.
- `app/build.gradle.kts`: currently applies Android application, Hilt, Compose, and KSP plugins; depends on `:core:core-ui`.
- `core/core-data/build.gradle.kts`: currently applies Android library, Hilt, and KSP; owns reusable data foundations.
- `app/src/main/java/com/aml_sakr/fitlife/FitnessApplication.kt`: currently only extends `Application` and is annotated with `@HiltAndroidApp`.
- `app/src/main/AndroidManifest.xml`: should continue to register `.FitnessApplication`.

### Architecture Compliance

- Keep Firebase setup app-owned: `google-services.json`, Google Services plugin, and Crashlytics Gradle plugin belong in `:app`, not library modules.
- Use `:core:core-data` for reusable Firebase-backed infrastructure abstractions and Hilt bindings. Do not add Firebase dependencies to `:core:core-domain` or `:core:core-ui`.
- Core modules must not depend on feature modules. Feature modules must not depend on data implementations directly through UI.
- Do not create Auth, Firestore repository, Room sync, or security-rules implementations here. Those are planned for AUTH-001, AUTH-004, SETUP-006, and later infrastructure stories.
- Keep package root `com.aml_sakr.fitlife`, `minSdk = 30`, `targetSdk = 36`, and Java 11 compatibility unchanged.
- Do not hardcode Firebase service account files, API secrets, Gemini keys, or private credentials. `google-services.json` contains Firebase app identifiers and is expected by the Google Services plugin, but no service-account JSON or private keys should enter the repo.

### Library and Framework Requirements

- Official Firebase Android setup currently recommends:
  - Google Services plugin `com.google.gms.google-services` version `4.4.4`.
  - Firebase BoM `com.google.firebase:firebase-bom:34.14.0`.
  - Main modules `com.google.firebase:firebase-analytics` and `com.google.firebase:firebase-crashlytics` with no explicit library versions when using the BoM.
- Official Crashlytics Android setup currently recommends:
  - Crashlytics Gradle plugin `com.google.firebase.crashlytics` version `3.0.7`.
  - Minimum required versions: Gradle `8.0`, Android Gradle plugin `8.1.0`, and Google Services plugin `4.4.1`. This project is already above these AGP/plugin requirements once Google Services is set to `4.4.4`.
  - Analytics enabled if breadcrumb logs are desired. This story should include Analytics because the project requires analytics event logging.
- Kotlin developers should use the main Firebase modules rather than Firebase KTX modules. Firebase stopped releasing new KTX module versions in July 2025 and removed KTX libraries from Firebase BoM `34.0.0`.
- Do not add Firebase Auth or Firestore SDKs in this story unless a later story requires them. Crashlytics and Analytics are enough for SETUP-003.

### Analytics and Crash Reporting Contract Guidance

- Prefer small contracts that later features can use without importing Firebase:
  - `AnalyticsLogger.logEvent(name: String, params: Map<String, Any?> = emptyMap())`
  - `CrashReporter.recordException(throwable: Throwable, keys: Map<String, String> = emptyMap())`
- Event parameter handling must support the basic Firebase Analytics parameter types the app will need later: `String`, numeric values, and `Boolean` converted safely.
- Keep event names stable and snake_case-compatible with the architecture taxonomy, but do not implement feature-specific event constants yet unless adding generic setup verification events.
- For tests, use fake implementations to verify callers can log events/record exceptions without Firebase initialization.

### Regression and Scope Guardrails

- Do not remove the starter `MainActivity` screen or change navigation.
- Do not add a visible "test crash" button to production UI.
- Do not call real Firebase services from unit tests.
- Do not make `:core:core-data` require a Firebase project to compile tests.
- Do not place `google-services.json` under `core/core-data` or any feature module.
- If a real Firebase project/config file is not available, the developer must stop and ask for the file rather than inventing fake IDs that make the build misleading.

### Testing Requirements

- Required command: `./gradlew.bat test --no-daemon --console=plain`.
- Required Firebase Gradle verification: `./gradlew.bat :app:assembleDebug --no-daemon --console=plain`.
- Prefer JVM tests for the core analytics/crash abstractions and fake implementations. Avoid instrumentation tests unless Firebase initialization behavior cannot be reasonably verified otherwise.
- If `google-services.json` is missing, `:app:assembleDebug` is expected to fail after applying the plugin. Treat that as a setup blocker requiring the real Firebase config file, not as a code issue to bypass.

### Previous Story Intelligence

- SETUP-002 established `Result`, `NetworkErrors`, `BaseRepository`, `SafeCall`, `PreferencesDataSource`, `CoreDataModule`, `UIState`, `Event`, `OneTimeAction`, and `BaseMviViewModel`. Reuse those boundaries.
- SETUP-002 moved shared theme ownership to `:core:core-ui`; do not recreate app-local theme files.
- SETUP-002 fixed tracked generated build artifacts and root `.gitignore` now excludes generated build/IDE files. Keep generated outputs out of commits.
- SETUP-002 verification passed `./gradlew.bat test` and `./gradlew.bat lint`; preserve that baseline.

### Git Intelligence

- Recent commits:
  - `14f08af SETUP-002`
  - `7a2b77c Implement setup 001 multi-module Gradle project`
  - `23398ae Implement setup 001 multi-module Gradle project`
  - `27da92d Implement setup 001 multi-module Gradle project`
  - `b180fa0 setup the project`
- Recent work has been setup-focused. Avoid introducing feature behavior in this setup story.

### References

- Epics: `_bmad-output/planning-artifacts/epics.md`
- PRD: `_bmad-output/planning-artifacts/fitlife-prd-v1.md`
- Architecture: `_bmad-output/planning-artifacts/fitlife-architecture-v1.md`
- UX spec: `docs/fitlife-ux-spec-v1.md`
- Project context: `_bmad-output/project-context.md`
- Previous story: `_bmad-output/implementation-artifacts/setup-002-core-domain-core-data-core-ui-boilerplate.md`
- Sprint status: `_bmad-output/implementation-artifacts/sprint-status.yaml`
- Firebase Android setup: https://firebase.google.com/docs/android/setup
- Crashlytics Android setup: https://firebase.google.com/docs/crashlytics/android/get-started
- Google Services Gradle plugin: https://developers.google.com/android/guides/google-services-plugin

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-06-03: Created SETUP-003 story context from sprint status, epics, PRD, architecture, UX spec, project context, current Gradle/app files, previous SETUP-002 story, recent git history, and current Firebase official setup docs.
- 2026-06-03: Started dev-story workflow and loaded project/story context. Implementation halted before code changes because required `app/google-services.json` is missing; story guardrails explicitly forbid inventing fake Firebase config.
- 2026-06-07: Resumed after real Firebase config was provided; moved root `google-services.json` to `app/google-services.json`.
- 2026-06-07: Added failing JVM tests for in-memory analytics and crash-reporting fakes; initial `:core:core-data:testDebugUnitTest` failed as expected before implementations existed.
- 2026-06-07: Added Firebase version catalog entries, app plugin/dependency wiring, core-data Firebase dependencies, observability abstractions, Firebase-backed implementations, in-memory test fakes, and Hilt bindings.
- 2026-06-07: Used Crashlytics Gradle plugin `3.0.6` because current official Crashlytics Android setup showed `3.0.6` during implementation; Firebase BoM remained `34.14.0` and Google Services plugin remained `4.4.4`.
- 2026-06-07: `./gradlew.bat :core:core-data:testDebugUnitTest --no-daemon --console=plain` passed after implementation.
- 2026-06-07: First `:app:assembleDebug` encountered stale Hilt generated output; `clean` plus rerun resolved it and `:app:assembleDebug` passed.
- 2026-06-07: `./gradlew.bat lint --no-daemon --console=plain` initially reported missing Firebase Analytics permissions in `:core:core-data`; added `INTERNET` and `WAKE_LOCK` to the core-data manifest and lint passed.
- 2026-06-07: Final validation passed: `./gradlew.bat test --no-daemon --console=plain`, `./gradlew.bat lint --no-daemon --console=plain`, and `./gradlew.bat :app:assembleDebug --no-daemon --console=plain`.
- 2026-06-07: Code review found two patch items: Crashlytics plugin pin mismatch and missing Firebase-backed wrapper tests. Both were fixed.
- 2026-06-07: Post-review validation passed: `./gradlew.bat :core:core-data:testDebugUnitTest --no-daemon --console=plain`, `./gradlew.bat :app:assembleDebug --no-daemon --console=plain`, `./gradlew.bat test --no-daemon --console=plain`, and `./gradlew.bat lint --no-daemon --console=plain`.

### Completion Notes List

- Story file created and marked ready-for-dev.
- Sprint status updated from backlog to ready-for-dev.
- Firebase versions researched from current official documentation and written into implementation guardrails.
- Firebase app configuration is present at `app/google-services.json`; no Firebase config was placed in library or feature modules.
- Firebase Gradle wiring is centralized in `gradle/libs.versions.toml`; Google Services and Crashlytics plugins are applied only in `:app`.
- `:app` now depends on `:core:core-data` so Hilt can provide reusable analytics/crash-reporting bindings.
- Added `AnalyticsLogger` and `CrashReporter` public contracts without Firebase SDK types in their API surface, plus Firebase-backed implementations and in-memory fakes for tests.
- `FitnessApplication` and app manifest startup behavior were preserved; no manual Firebase initialization, debug crash UI, Auth, Firestore, sync, security rules, service-account credentials, or feature-specific analytics events were added.
- Core-data manifest now declares the permissions required by Firebase Analytics lint checks.
- Review findings resolved: Crashlytics Gradle plugin is pinned to `3.0.7`, and Firebase-backed analytics/crash wrapper behavior is covered with JVM-safe sink fakes.

### File List

- `_bmad-output/implementation-artifacts/setup-003-firebase-crashlytics-integration.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `google-services.json` (moved)
- `app/google-services.json`
- `gradle/libs.versions.toml`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `core/core-data/build.gradle.kts`
- `core/core-data/src/main/AndroidManifest.xml`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/di/FirebaseObservabilityModule.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/observability/AnalyticsLogger.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/observability/CrashReporter.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/observability/FirebaseAnalyticsLogger.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/observability/FirebaseCrashReporter.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/observability/InMemoryAnalyticsLogger.kt`
- `core/core-data/src/main/java/com/aml_sakr/fitlife/core/data/observability/InMemoryCrashReporter.kt`
- `core/core-data/src/test/java/com/aml_sakr/fitlife/core/data/observability/AnalyticsLoggerTest.kt`
- `core/core-data/src/test/java/com/aml_sakr/fitlife/core/data/observability/CrashReporterTest.kt`

### Change Log

- 2026-06-03: Created comprehensive SETUP-003 story context and marked story ready for development.
- 2026-06-03: Started implementation; blocked pending real Firebase `app/google-services.json`.
- 2026-06-07: Implemented SETUP-003 Firebase Analytics/Crashlytics integration and moved story to review.
- 2026-06-07: Addressed code review findings and moved story to done.
