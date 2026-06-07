package com.aml_sakr.fitlife.feature.workout.data.gemini

import com.google.gson.Gson
import com.google.gson.JsonParser

class GeminiPlanResponseParser(
    private val gson: Gson = Gson()
) {
    fun parse(responseBody: String): GeminiParsedPlanResult {
        return try {
            val root = JsonParser.parseString(responseBody).asJsonObject
            val text = root.getAsJsonArray("candidates")
                ?.firstOrNull()
                ?.asJsonObject
                ?.getAsJsonObject("content")
                ?.getAsJsonArray("parts")
                ?.firstOrNull()
                ?.asJsonObject
                ?.get("text")
                ?.asString
                ?: return invalidResult()
            val plan = gson.fromJson(stripMarkdownFence(text), GeminiWorkoutPlanDraft::class.java)
            val usage = root.getAsJsonObject("usageMetadata")
            GeminiParsedPlanResult(
                plan = plan,
                promptTokenCount = usage?.get("promptTokenCount")?.asInt,
                candidatesTokenCount = usage?.get("candidatesTokenCount")?.asInt,
                isValidPlan = isValidPlan(plan)
            )
        } catch (_: Exception) {
            invalidResult()
        }
    }

    private fun invalidResult() = GeminiParsedPlanResult(
        plan = null,
        promptTokenCount = null,
        candidatesTokenCount = null,
        isValidPlan = false
    )

    private fun stripMarkdownFence(text: String): String {
        val trimmed = text.trim()
        return if (trimmed.startsWith("```")) {
            trimmed
                .lineSequence()
                .filterNot { it.trim().startsWith("```") }
                .joinToString(separator = "\n")
                .trim()
        } else {
            trimmed
        }
    }

    private fun isValidPlan(plan: GeminiWorkoutPlanDraft?): Boolean {
        if (plan == null || plan.days.size != 7) return false
        return plan.days.all { day ->
            day.day in 1..7 &&
                day.title.isNotBlank() &&
                day.durationMinutes > 0 &&
                day.exercises.isNotEmpty() &&
                day.exercises.all { exercise ->
                    exercise.name.isNotBlank() &&
                        exercise.sets > 0 &&
                        exercise.reps.isNotBlank() &&
                        exercise.estimatedDurationMinutes > 0
                }
        }
    }
}
