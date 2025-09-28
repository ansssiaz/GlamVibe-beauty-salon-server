package com.glamvibe.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Int = 0,
    val name: String = "",
    val services: List<Service> = emptyList(),
)

@Serializable
data class Service(
    val id: Int = 0,
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val duration: Int = 0,
    val price: Int = 0,
    val promotionId: Int? = null,
    val promotionName: String? = null,
    val priceWithPromotion: Int? = null,
    val discountPercentage: Int? = null,
)
