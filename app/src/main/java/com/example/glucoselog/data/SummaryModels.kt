package com.example.glucoselog.data

data class SummaryStats(
    val count: Int = 0,
    val avgCgm: Double = 0.0,
    val avgManual: Double = 0.0,
    val avgDifference: Double = 0.0,
    val maxCgm: Int = 0,
    val minCgm: Int = 0,
    val maxManual: Int = 0,
    val minManual: Int = 0,
    val contextCounts: Map<String, Int> = emptyMap()
)
