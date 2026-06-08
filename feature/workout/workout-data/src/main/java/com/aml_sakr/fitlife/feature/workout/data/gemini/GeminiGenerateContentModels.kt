package com.aml_sakr.fitlife.feature.workout.data.gemini

data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val responseMimeType: String,
    val responseSchema: GeminiResponseSchema,
    val temperature: Double,
    val maxOutputTokens: Int
)

data class GeminiResponseSchema(
    val type: String,
    val properties: Map<String, GeminiResponseSchema> = emptyMap(),
    val items: GeminiResponseSchema? = null,
    val required: List<String> = emptyList()
)

data class GeminiApiCallResult(
    val httpStatusCode: Int,
    val responseBody: String,
    val responseSizeChars: Int
)

data class GeminiParsedPlanResult(
    val plan: GeminiWorkoutPlanDraft?,
    val promptTokenCount: Int?,
    val candidatesTokenCount: Int?,
    val isValidPlan: Boolean
)

data class GeminiWorkoutPlanDraft(
    val days: List<GeminiWorkoutDayDraft>
)

data class GeminiWorkoutDayDraft(
    val day: Int,
    val title: String,
    val durationMinutes: Int,
    val exercises: List<GeminiWorkoutExerciseDraft>
)

data class GeminiWorkoutExerciseDraft(
    val name: String,
    val sets: Int,
    val reps: String,
    val estimatedDurationMinutes: Int
)
