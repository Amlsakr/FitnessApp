package com.aml_sakr.fitlife.feature.workout.data.gemini

import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkConfiguration
import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiWorkoutProfile

class GeminiWorkoutPromptBuilder {
    fun buildRequest(
        profile: GeminiWorkoutProfile,
        configuration: GeminiBenchmarkConfiguration
    ): GeminiGenerateContentRequest {
        val prompt = """
            Generate a personalized 7-day workout plan for FitLife.
            Return only JSON matching the provided schema.
            Profile id: ${profile.id}
            Fitness level: ${profile.fitnessLevel}
            Goal: ${profile.goal}
            Location: ${profile.location}
            Available equipment: ${profile.availableEquipment.joinToString()}
            Days requested: ${profile.days}
            Each day must include day number, title, durationMinutes, and at least one exercise with name, sets, reps, and estimatedDurationMinutes.
        """.trimIndent()

        return GeminiGenerateContentRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = configuration.responseMimeType,
                responseSchema = workoutPlanSchema(),
                temperature = configuration.temperature,
                maxOutputTokens = configuration.maxOutputTokens
            )
        )
    }

    private fun workoutPlanSchema(): GeminiResponseSchema {
        val exerciseSchema = GeminiResponseSchema(
            type = "object",
            properties = mapOf(
                "name" to GeminiResponseSchema(type = "string"),
                "sets" to GeminiResponseSchema(type = "integer"),
                "reps" to GeminiResponseSchema(type = "string"),
                "estimatedDurationMinutes" to GeminiResponseSchema(type = "integer")
            ),
            required = listOf("name", "sets", "reps", "estimatedDurationMinutes")
        )
        val daySchema = GeminiResponseSchema(
            type = "object",
            properties = mapOf(
                "day" to GeminiResponseSchema(type = "integer"),
                "title" to GeminiResponseSchema(type = "string"),
                "durationMinutes" to GeminiResponseSchema(type = "integer"),
                "exercises" to GeminiResponseSchema(
                    type = "array",
                    items = exerciseSchema
                )
            ),
            required = listOf("day", "title", "durationMinutes", "exercises")
        )
        return GeminiResponseSchema(
            type = "object",
            properties = mapOf(
                "days" to GeminiResponseSchema(
                    type = "array",
                    items = daySchema
                )
            ),
            required = listOf("days")
        )
    }
}
