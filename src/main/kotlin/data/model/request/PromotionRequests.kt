package com.glamvibe.data.model.request

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


@Serializable
data class NewPromotion(
    val name: String = "",
    @Contextual val startDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    @Contextual val endDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val image: String = "",
    val description: String = "",
    val services: List<PromotionService> = emptyList(),
)

@Serializable
data class PromotionService(
    val id: Int,
    val priceWithPromotion: Int,
    val discountPercentage: Int
)
