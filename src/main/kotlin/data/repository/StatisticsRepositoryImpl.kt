package com.glamvibe.data.repository

import com.glamvibe.domain.model.Statistics
import com.glamvibe.domain.repository.StatisticsRepository

class StatisticsRepositoryImpl: StatisticsRepository {
    override suspend fun getStatisticsByMonth(): Statistics {
        TODO("Not yet implemented")
    }

    override suspend fun createStatisticsForMonth(): Statistics {
        TODO("Not yet implemented")
    }
}