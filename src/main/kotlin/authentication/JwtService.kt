package com.glamvibe.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.glamvibe.domain.model.User
import com.glamvibe.domain.model.getStringByRole
import com.glamvibe.data.model.response.TokenPair
import java.time.Duration
import java.util.*

class JwtConfig(
    val issuer: String,
    jwtSecret: String,
) {
    val algorithm: Algorithm = Algorithm.HMAC512(jwtSecret)
}

class JwtService(
    private val config: JwtConfig,
    private val accessLifetime: Long,
    private val refreshLifetime: Long
) {
    fun generateTokenPair(user: User): TokenPair {
        val currentTime = System.currentTimeMillis()
        val accessToken = JWT.create()
            .withSubject("glamvibe-server-authentication")
            .withIssuer(config.issuer)
            .withClaim("id", user.id)
            .withClaim("role", user.role.getStringByRole())
            .withExpiresAt(Date(currentTime + Duration.ofMinutes(accessLifetime).toMillis()))
            .sign(config.algorithm)

        val refreshToken = UUID.randomUUID().toString()
        return TokenPair(accessToken, refreshToken)
    }

    fun setExpirationTime(): Long {
        val currentTime = System.currentTimeMillis()
        return Date(currentTime + Duration.ofDays(refreshLifetime).toMillis()).time
    }
}