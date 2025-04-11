package com.glamvibe.plugins

import com.auth0.jwt.JWT
import com.glamvibe.authentication.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity(jwtConfig: JwtConfig) {
    authentication {
        jwt {
            verifier(
                JWT
                    .require(jwtConfig.algorithm)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )
            validate {
                val currentTime = System.currentTimeMillis()
                if (it.payload.expiresAt.time > currentTime) {
                    return@validate JWTPrincipal(it.payload)
                } else null
            }
        }
    }
}
