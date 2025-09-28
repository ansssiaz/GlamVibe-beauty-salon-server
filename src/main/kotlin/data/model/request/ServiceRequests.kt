package com.glamvibe.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class NewService(
    val name: String = "",
    val categoryId: Int = 0,
    val description: String = "",
    val image: String = "",
    val duration: Int = 0,
    val price: Int = 0,
)