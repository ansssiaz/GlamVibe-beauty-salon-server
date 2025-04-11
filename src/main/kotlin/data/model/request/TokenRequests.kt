package com.glamvibe.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class RefreshToken(
    val refreshToken: String,
)