package com.glamvibe.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class MonthlyStatistics(
    val id: Long = 0L,
    @Contextual val creationDate: LocalDate = LocalDate.now(),
    val year: Int = 2025,
    val month: Int = 1,
    val totalAppointmentsCount: Int = 0,
    val newClientsAppointmentsCount: Int = 0,
    val returningClientsAppointmentsCount: Int = 0,
    val cancellationsCount: Int = 0,
    val totalRevenue: Int = 0,
    val averageTicket: Double = 0.0,
    val categoriesStatistics: List<CategoryStatistics> = emptyList(),
    val promotionsStatistics: List<PromotionStatistics?> = emptyList<PromotionStatistics>(),
    val popularServices: List<Pair<Int, Int>> = emptyList(),
    val popularMasters: List<Pair<Int, Int>> = emptyList(),
)

@Serializable
data class CategoryStatistics(
    val categoryId: Long = 0L,
    @Contextual val creationDate: LocalDate = LocalDate.now(),
    val year: Int = 2025,
    val month: Int = 1,
    val totalAppointmentsCount: Int = 0,
    val newClientsAppointmentsCount: Int = 0,
    val returningClientsAppointmentsCount: Int = 0,
    val cancellationsCount: Int = 0,
    val totalRevenue: Int = 0,
    val averageTicket: Double = 0.0,
    val popularServices: List<Pair<Int, Int>> = emptyList(),
    val popularMasters: List<Pair<Int, Int>> = emptyList(),
)

@Serializable
data class PromotionStatistics(
    val promotionId: Long = 0L,
    @Contextual val creationDate: LocalDate = LocalDate.now(),
    val year: Int = 2025,
    val month: Int = 1,
    val totalAppointmentsCount: Int = 0,
    val newClientsAppointmentsCount: Int = 0,
    val returningClientsAppointmentsCount: Int = 0,
    val cancellationsCount: Int = 0,
    val totalRevenue: Int = 0,
    val averageTicket: Double = 0.0,
)
