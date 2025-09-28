package com.glamvibe.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


@Serializable
data class Promotion(
    val id: Int = 0,
    val name: String = "",
    @Contextual val startDate: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date,
    @Contextual val endDate: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val imageUrl: String = "",
    val description: String = "",
    val isCurrent: Boolean = false,
    val services: List<Service> = emptyList(),
)
