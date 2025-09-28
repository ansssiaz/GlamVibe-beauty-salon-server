package com.glamvibe.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ClientService(
    val id: Int,
    val name: String,
    val category: String,
    val description: String,
    val imageUrl: String,
    val duration: Int,
    val price: Int,
    val priceWithPromotion: Int? = null,
    val discountPercentage: Int? = null,
    val isFavourite: Boolean = false,
)

@Serializable
data class CategoryResponse(
    val id: Int,
    val name: String,
    val services: List<String> = emptyList(),
)