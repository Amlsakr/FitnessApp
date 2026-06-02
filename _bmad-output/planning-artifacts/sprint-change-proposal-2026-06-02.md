# Sprint Change Proposal - 2026-06-02

## 1. Issue Summary

Story `setup-001` specified package namespaces under `com.aml_sakr.fitnessapp`, but the baseline Android project and desired app identity use `com.aml_sakr.fitlife`.

The issue was identified after implementation began and package/application identifiers had been moved to `com.aml_sakr.fitnessapp`. This created avoidable drift from the existing project package and the user-confirmed namespace.

## 2. Impact Analysis

Epic impact: No MVP or epic scope change is required. Epic 0 remains valid as project foundation work.

Story impact: `setup-001` needed a direct correction in its namespace guidance and file references. Future stories should use `com.aml_sakr.fitlife` for app, core, and feature packages.

Architecture impact: Clean Architecture, MVI, Hilt, module boundaries, and dependency graph remain unchanged. Only package namespace strings and source paths are affected.

PRD and UX impact: No product, UI, user flow, or MVP behavior changes are required.

Technical impact: Gradle `namespace`, app `applicationId`, Kotlin package declarations, test package expectations, source directory paths, and project context guidance were updated to `com.aml_sakr.fitlife`.

## 3. Recommended Approach

Recommended path: Direct Adjustment.

Rationale: The change is low risk, localized, and does not require backlog reorganization or architectural redesign. Keeping the existing app identity avoids churn in Android package references and aligns implementation with the user-confirmed namespace.

Effort: Low.
Risk: Low.
Timeline impact: None beyond retesting.

## 4. Detailed Change Proposals

Story: `setup-001`
Section: Tasks / Subtasks

OLD:
```text
- [ ] Keep package namespaces under `com.aml_sakr.fitnessapp`.
```

NEW:
```text
- [ ] Keep package namespaces under `com.aml_sakr.fitlife`.
```

Rationale: `com.aml_sakr.fitlife` is the desired canonical app namespace.

Story: `setup-001`
Section: Dev Notes / File Structure Requirements / References

OLD:
```text
app/src/main/java/com/aml_sakr/fitnessapp/MainActivity.kt
com.aml_sakr.fitnessapp.core.ui.mvi
com.aml_sakr.fitnessapp.core.domain
```

NEW:
```text
app/src/main/java/com/aml_sakr/fitlife/MainActivity.kt
com.aml_sakr.fitlife.core.ui.mvi
com.aml_sakr.fitlife.core.domain
```

Rationale: Story guidance should point developers to the actual package and source path.

Project Context:
Section: Language-Specific Rules

OLD:
```text
Keep application code under package `com.aml_sakr.fitnessapp` unless a new module/package boundary is intentionally introduced.
```

NEW:
```text
Keep application code under package `com.aml_sakr.fitlife` unless a new module/package boundary is intentionally introduced.
```

Rationale: Persistent agent context must match the corrected namespace to prevent future drift.

Implementation:
Update all source package declarations, source directories, Gradle namespaces, and `applicationId` from `com.aml_sakr.fitnessapp` to `com.aml_sakr.fitlife`.

## 5. Implementation Handoff

Scope classification: Minor.

Routed to: Developer agent for direct implementation.

Success criteria:
- No remaining `com.aml_sakr.fitnessapp` references in source/story/context files.
- `setup-001.md` and project context both reference `com.aml_sakr.fitlife`.
- Gradle tests pass after namespace correction.

## 6. Workflow Notes

Checklist summary:
- Trigger/context: Done.
- Epic impact: No scope change.
- Artifact conflicts: Story and project context required updates.
- Path forward: Direct Adjustment selected.
- Handoff: Developer agent implemented and verified.

Verification:
- `.\gradlew.bat test` passed after the correction.
