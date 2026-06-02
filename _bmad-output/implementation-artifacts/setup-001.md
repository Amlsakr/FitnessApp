# Story setup-001: Project Foundation and Modular Architecture Setup

Status: review

Completion Note: Ultimate context engine analysis completed - comprehensive developer guide created.

## Story

As the FitLife developer,
I want the Android project foundation to match the approved modular Clean Architecture and MVI architecture,
so that auth, onboarding, workout, session, and progress features can be implemented without build churn or structural rework.

## Acceptance Criteria

1. Given the repository currently includes only `:app`, when Gradle sync runs, then the project includes the app module plus the documented core and feature modules:
   `:core:core-data`, `:core:core-domain`, `:core:core-ui`,
   `:feature:auth:auth-data`, `:feature:auth:auth-domain`, `:feature:auth:auth-ui`,
   `:feature:onboarding:onboarding-data`, `:feature:onboarding:onboarding-domain`, `:feature:onboarding:onboarding-ui`,
   `:feature:workout:workout-data`, `:feature:workout:workout-domain`, `:feature:workout:workout-ui`,
   `:feature:session:session-data`, `:feature:session:session-domain`, `:feature:session:session-ui`,
   `:feature:progress:progress-data`, `:feature:progress:progress-domain`, and `:feature:progress:progress-ui`.
2. Given the architecture dependency graph, when module dependencies are declared, then core modules do not depend on feature modules and each feature module depends only on the matching sibling layer plus approved core modules.
3. Given app code uses Jetpack Compose, when the baseline app launches, then `MainActivity` remains a single-activity Compose host and uses the existing `FitnessAppTheme`.
4. Given future stories require MVI, when the foundation is complete, then shared MVI contracts and a reusable base ViewModel exist in the appropriate core module and expose unidirectional data flow: Compose UI -> Intent/Event -> ViewModel -> State -> Compose UI, with one-time actions separated from persistent state.
5. Given future domain layers must return wrapped results, when the foundation is complete, then shared domain result/error contracts exist and can support repository methods such as `IAuthRepository`, `IWorkoutRepository`, `ISessionRepository`, `IProgressRepository`, and `IOnboardingRepository`.
6. Given dependency injection is part of the target stack, when setup is complete, then Hilt is configured for the app and modules without creating feature-specific implementation logic prematurely.
7. Given the project must stay releasable through incremental setup, when tests run, then `./gradlew test` passes and at least one focused unit test validates the shared MVI base behavior or result contract.

## Tasks / Subtasks

- [x] Align Gradle settings with the approved module graph. (AC: 1, 2)
  - [x] Update `settings.gradle.kts` to include all core and feature modules from the architecture document.
  - [x] Add minimal `build.gradle.kts` files for each new module using Android library or Kotlin/JVM plugins as appropriate.
  - [x] Keep package namespaces under `com.aml_sakr.fitlife`.
- [x] Centralize dependency versions in `gradle/libs.versions.toml`. (AC: 6, 7)
  - [x] Add aliases for Android library modules, Hilt, lifecycle ViewModel Compose support, coroutines test support, and any test libraries used by the foundation.
  - [x] Use fixed versions only; do not use dynamic `+` dependency versions.
  - [x] Keep the existing Compose BOM approach for Compose artifacts.
- [x] Create core architecture contracts. (AC: 4, 5)
  - [x] In `core-domain`, create shared result and error contracts compatible with repository return types in the architecture doc.
  - [x] In `core-ui`, create MVI marker contracts for UI state, UI events/intents, and one-time actions.
  - [x] In `core-ui`, add a base ViewModel that owns immutable state, accepts events/intents, and emits one-time actions through a coroutine stream.
- [x] Prepare app-level hosting and dependency injection. (AC: 3, 6)
  - [x] Add an `Application` class annotated for Hilt and register it in `AndroidManifest.xml`.
  - [x] Apply Hilt to `:app`; add only foundation DI modules required by shared setup.
  - [x] Preserve `MainActivity` as the Compose single-activity host and keep `FitnessAppTheme`.
- [x] Add verification coverage. (AC: 7)
  - [x] Add unit coverage for the shared result contract or MVI base ViewModel.
  - [x] Run `./gradlew test`.
  - [x] Run a Gradle sync/build task if test setup reveals configuration issues.

## Dev Notes

### Current State

- The repository is an Android Gradle project named `FitnessApp` with only `:app` included. [Source: settings.gradle.kts]
- The app module currently contains a starter Compose `MainActivity` that renders `Greeting("Android")` inside `FitnessAppTheme`. Preserve this as the smoke-test entry point unless the setup requires a thin app shell replacement. [Source: app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt]
- The app already uses Kotlin, Jetpack Compose, Material3, `enableEdgeToEdge`, version catalogs, Gradle Wrapper 9.4.1, AGP 9.2.1, Kotlin 2.2.10, and Compose BOM 2026.02.01. [Source: gradle/libs.versions.toml] [Source: app/build.gradle.kts] [Source: gradle/wrapper/gradle-wrapper.properties]
- There is no sprint-status file or epic story file in this worktree, so this story is derived from the PRD, architecture document, current source tree, and the explicit user-provided key `setup-001`.

### Product Context

- FitLife is an Android-only AI personal health coach for Egypt/MENA with Firebase profiles, differentiated onboarding, Gemini-generated workout plans, guided sessions, ML Kit pose feedback, lighting fallback, fatigue analysis, and progress analytics. [Source: docs/fitlife-prd-v1.md#1-executive-summary]
- MVP implementation is planned around Kotlin, Jetpack Compose, MVI, coroutines, Room, Retrofit, Hilt, ML Kit, Firebase, and MPAndroidChart. [Source: docs/fitlife-prd-v1.md#1-executive-summary]
- Android compatibility in the PRD says Android 26+, but the existing app has `minSdk = 30`. Do not lower minSdk in this story unless product/architecture explicitly approves the change; note the variance in completion notes. [Source: docs/fitlife-prd-v1.md#6-non-functional-requirements] [Source: app/build.gradle.kts]

### Architecture Compliance

- Follow Clean Architecture module boundaries exactly:
  - `*-domain` contains pure Kotlin domain models, repository interfaces, use cases, and result/error contracts. It must not depend on Android UI, Retrofit, Room, Firebase, or feature UI.
  - `*-data` contains data sources, DTO/entity mapping, repository implementations, Room/Firestore/Retrofit integration, and depends on its own domain plus core data.
  - `*-ui` contains Compose UI, state/event/action classes, ViewModels, and navigation entry points, and depends on its own domain plus core UI.
  - `core-domain`, `core-data`, and `core-ui` must not depend on feature modules. [Source: docs/fitlife-architecture-v1.md#1-module-structure--gradle-dependency-graph]
- Future feature implementation expects MVI naming and direction:
  - State: immutable data class implementing `UIState`.
  - Event or intent: sealed type implementing `Event`.
  - One-time action: sealed type implementing `OneTimeAction`.
  - ViewModel: extends shared base, exposes state, handles events, and sends actions. [Source: docs/fitlife-architecture-v1.md#2-mvi-implementation-example-workout-feature]
- Use cases should follow the single `operator fun invoke()` pattern and depend on repository interfaces, not concrete implementations. [Source: docs/fitlife-architecture-v1.md#6-use-case-implementations-domain]

### File Structure Requirements

- Update existing files:
  - `settings.gradle.kts`: add module includes while preserving plugin management and repository settings.
  - `build.gradle.kts`: add shared plugin aliases with `apply false`; keep app plugin declarations intact.
  - `gradle/libs.versions.toml`: add version aliases and library/plugin aliases in the existing catalog style.
  - `app/build.gradle.kts`: apply Hilt and app dependencies required for the foundation.
  - `app/src/main/AndroidManifest.xml`: register the Hilt application class.
  - `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`: preserve single-activity Compose host.
- New files should live in module paths that match Gradle includes. Do not place core contracts inside `:app`; future feature modules must be able to consume them without depending on the app module.
- Use package names that mirror module responsibility, for example `com.aml_sakr.fitlife.core.ui.mvi` and `com.aml_sakr.fitlife.core.domain`.

### Library and Framework Requirements

- Use Kotlin and Jetpack Compose for UI. Android's May 2026 guidance says Android UI development is "Compose First" and new UI tools/guidance focus on Compose, so do not introduce XML View-based architecture for this foundation. [Source: Android Developers Blog, Android UI Development is Compose First, 2026-05-19: https://android-developers.googleblog.com/2026/05/android-ui-development-is-compose-first.html]
- Keep Compose dependency versions managed by the stable Compose BOM. Android docs describe the stable BOM as containing latest stable versions of each Compose library; alpha/beta BOMs are for testing and not intended for production. [Source: Android Developers Compose BOM docs, last updated 2026-05-18: https://developer.android.com/develop/ui/compose/bom]
- AGP 9.2 requires Gradle 9.4.1 or newer, and this project already uses Gradle Wrapper 9.4.1. Keep that compatibility intact. [Source: Android Developers AGP docs: https://developer.android.com/build/releases/about-agp] [Source: gradle/wrapper/gradle-wrapper.properties]
- Android's AGP docs warn against dynamic dependency versions because they cause unexpected updates and version resolution difficulty. All new aliases must be pinned. [Source: Android Developers AGP docs: https://developer.android.com/build/releases/about-agp]
- Do not add Room, Retrofit, Firebase Auth, Firestore, ML Kit, Gemini API, CameraX, or MPAndroidChart implementation code in this setup story unless needed only as dependency placeholders agreed by the build structure. Those belong to feature stories.

### Regression and Scope Guardrails

- Do not implement onboarding, auth screens, workout generation, pose detection, lighting fallback, fatigue detection, progress charts, or navigation flows in this story.
- Do not replace MVI with MVVM. The PRD explicitly confirms MVI + Clean Architecture throughout. [Source: docs/fitlife-prd-v1.md#10-open-questions-for-architect-agent]
- Do not make feature modules depend on `:app`.
- Do not use a shared feature module that blends auth/onboarding/workout/session/progress code.
- Do not remove the existing starter tests unless replacing them with meaningful setup tests.
- Keep Firebase/Gemini credentials out of source control. This story should not introduce API keys.

### Testing Requirements

- Required command: `./gradlew test`.
- Add focused JVM unit tests for foundation contracts. If testing ViewModel state/action behavior, use coroutine test utilities and avoid Android instrumentation unless needed.
- The app should remain buildable from a clean checkout after Gradle downloads dependencies.
- Manual smoke check: app launch still reaches `MainActivity` and renders Compose content.

### Git Intelligence

- Repository history contains only one commit: `040f783 Initial commit`. There are no previous story implementation patterns to reuse yet.

### References

- PRD: `docs/fitlife-prd-v1.md`
- Architecture: `docs/fitlife-architecture-v1.md`
- Current Gradle settings: `settings.gradle.kts`
- Version catalog: `gradle/libs.versions.toml`
- App build file: `app/build.gradle.kts`
- Main activity: `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- Android Gradle Plugin docs: https://developer.android.com/build/releases/about-agp
- Compose BOM docs: https://developer.android.com/develop/ui/compose/bom
- Compose First announcement: https://android-developers.googleblog.com/2026/05/android-ui-development-is-compose-first.html

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-06-02: Loaded BMad workflow customization, project config, project context, explicit story file, and sprint status.
- 2026-06-02: Verified module graph, Gradle aliases, app Hilt setup, shared MVI/domain contracts, and focused MVI ViewModel test coverage.
- 2026-06-02: Ran `./gradlew.bat test` successfully. Build result: `BUILD SUCCESSFUL in 22s`; 169 tasks up-to-date.
- 2026-06-02: Ran final `./gradlew.bat test` after story record updates. Build result: `BUILD SUCCESSFUL in 17s`; 169 tasks up-to-date.
- 2026-06-02: Sprint tracker key did not exactly match the explicit story filename `setup-001.md`; story file status was updated to `review` and sprint-status was left unchanged.

### Completion Notes List

- Implemented the approved multi-module Clean Architecture scaffold for app, core, and auth/onboarding/workout/session/progress feature layers.
- Added centralized Gradle aliases for Android library modules, Hilt, lifecycle ViewModel support, coroutine testing, and test dependencies with fixed versions and Compose artifacts still managed through the Compose BOM.
- Added shared `DomainResult` and `DomainError` contracts in `core-domain`.
- Added shared MVI marker contracts and `BaseMviViewModel` in `core-ui`, with immutable state exposure and one-time actions emitted through a coroutine flow.
- Added Hilt application setup while preserving `MainActivity` as the single-activity Compose host using `FitnessAppTheme`.
- Added focused JVM unit coverage for the shared MVI base behavior.
- Kept `minSdk = 30`; this remains different from the PRD's Android 26+ note as requested in Dev Notes.

### File List

- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle.properties`
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/aml_sakr/fitlife/FitnessApplication.kt`
- `app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt`
- `app/src/main/java/com/aml_sakr/fitlife/ui/theme/Color.kt`
- `app/src/main/java/com/aml_sakr/fitlife/ui/theme/Theme.kt`
- `app/src/main/java/com/aml_sakr/fitlife/ui/theme/Type.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/test/java/com/aml_sakr/fitlife/ExampleUnitTest.kt`
- `app/src/androidTest/java/com/aml_sakr/fitlife/ExampleInstrumentedTest.kt`
- `core/core-data/build.gradle.kts`
- `core/core-domain/build.gradle.kts`
- `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/DomainError.kt`
- `core/core-domain/src/main/java/com/aml_sakr/fitlife/core/domain/DomainResult.kt`
- `core/core-ui/build.gradle.kts`
- `core/core-ui/src/main/java/com/aml_sakr/fitlife/core/ui/mvi/BaseMviViewModel.kt`
- `core/core-ui/src/main/java/com/aml_sakr/fitlife/core/ui/mvi/MviContracts.kt`
- `core/core-ui/src/test/java/com/aml_sakr/fitlife/core/ui/mvi/BaseMviViewModelTest.kt`
- `feature/auth/auth-data/build.gradle.kts`
- `feature/auth/auth-domain/build.gradle.kts`
- `feature/auth/auth-ui/build.gradle.kts`
- `feature/onboarding/onboarding-data/build.gradle.kts`
- `feature/onboarding/onboarding-domain/build.gradle.kts`
- `feature/onboarding/onboarding-ui/build.gradle.kts`
- `feature/workout/workout-data/build.gradle.kts`
- `feature/workout/workout-domain/build.gradle.kts`
- `feature/workout/workout-ui/build.gradle.kts`
- `feature/session/session-data/build.gradle.kts`
- `feature/session/session-domain/build.gradle.kts`
- `feature/session/session-ui/build.gradle.kts`
- `feature/progress/progress-data/build.gradle.kts`
- `feature/progress/progress-domain/build.gradle.kts`
- `feature/progress/progress-ui/build.gradle.kts`

### Change Log

- 2026-06-02: Completed modular Gradle foundation, shared core architecture contracts, Hilt app setup, and MVI base ViewModel coverage; moved story to review.
