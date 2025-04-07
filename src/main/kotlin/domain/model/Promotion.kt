package com.glamvibe.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Promotion(
    val id: Int = 0,
    val name: String = "",
    @Contextual val startDate: LocalDate = LocalDate.now(),
    @Contextual val endDate: LocalDate = LocalDate.now(),
    val imageUrl: String = "",
    val description: String = "",
    val isCurrent: Boolean = false,
    val services: List<Service> = emptyList(),
)
