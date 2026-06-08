package com.aml_sakr.fitlife.feature.workout.data.gemini

import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiWorkoutProfile

interface GeminiFallbackPlanProvider {
    fun fallbackPlanPath(profile: GeminiWorkoutProfile): String
}

object StaticGeminiFallbackPlanProvider : GeminiFallbackPlanProvider {
    override fun fallbackPlanPath(profile: GeminiWorkoutProfile): String =
        "assets/fallback_workout_plans.json"
}
