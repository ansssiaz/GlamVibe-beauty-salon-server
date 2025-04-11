package com.glamvibe.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
)