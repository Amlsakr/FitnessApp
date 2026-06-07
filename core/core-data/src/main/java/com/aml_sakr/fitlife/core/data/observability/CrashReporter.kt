package com.aml_sakr.fitlife.core.data.observability

interface CrashReporter {
    fun recordException(
        throwable: Throwable,
        keys: Map<String, String> = emptyMap()
    )
}
