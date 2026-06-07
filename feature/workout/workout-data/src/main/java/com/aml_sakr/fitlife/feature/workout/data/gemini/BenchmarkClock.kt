package com.aml_sakr.fitlife.feature.workout.data.gemini

interface BenchmarkClock {
    fun nowMillis(): Long
}

object SystemBenchmarkClock : BenchmarkClock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
