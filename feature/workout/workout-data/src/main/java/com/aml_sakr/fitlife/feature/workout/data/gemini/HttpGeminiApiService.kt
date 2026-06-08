package com.aml_sakr.fitlife.feature.workout.data.gemini

import com.aml_sakr.fitlife.feature.workout.domain.gemini.GeminiBenchmarkConfiguration
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class HttpGeminiApiService(
    private val gson: Gson = Gson()
) : GeminiApiService {
    override suspend fun generatePlan(
        request: GeminiGenerateContentRequest,
        apiKey: String,
        configuration: GeminiBenchmarkConfiguration
    ): GeminiApiCallResult = withContext(Dispatchers.IO) {
        val modelPath = configuration.modelName.removePrefix("models/")
        val url = URL("https://generativelanguage.googleapis.com/${configuration.apiVersion}/models/$modelPath:generateContent")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = configuration.timeoutMillis.toInt()
            readTimeout = configuration.timeoutMillis.toInt()
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-goog-api-key", apiKey)
        }

        try {
            val body = gson.toJson(request).toByteArray(Charsets.UTF_8)
            connection.outputStream.use { it.write(body) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() } ?: ""
            GeminiApiCallResult(
                httpStatusCode = status,
                responseBody = response,
                responseSizeChars = response.length
            )
        } finally {
            connection.disconnect()
        }
    }
}
