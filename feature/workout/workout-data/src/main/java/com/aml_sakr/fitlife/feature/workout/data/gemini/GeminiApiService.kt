package com.aml_sakr.fitlife.feature.workout.data.gemini

import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkConfiguration

interface GeminiApiService {
    suspend fun generatePlan(
        request: GeminiGenerateContentRequest,
        apiKey: String,
        configuration: GeminiBenchmarkConfiguration
    ): GeminiApiCallResult
}
