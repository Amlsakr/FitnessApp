package com.aml_sakr.fitlife.core.data.observability

interface AnalyticsLogger {
    fun logEvent(
        name: String,
        params: Map<String, Any?> = emptyMap()
    )
}
