package com.glamvibe.domain.repository

import com.glamvibe.domain.model.Statistics

interface StatisticsRepository {
    suspend fun getStatisticsByMonth(): Statistics
    suspend fun createStatisticsForMonth(): Statistics
}