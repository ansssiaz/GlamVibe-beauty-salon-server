package com.glamvibe.plugins

import com.glamvibe.data.repository.PostgresUserRepository
import io.ktor.server.application.*

fun Application.configureSecurity() {

    /*
    val jwtService = JwtService()
    val userRepository = PostgresUserRepository()
    val userUseCase = userUseCase(userRepository, jwtService)
     */

/*    // Please read the jwt property from the config file if you are using EngineMain
    val jwtAudience = "jwt-audience"
    val jwtDomain = "https://jwt-provider-domain/"
    val jwtRealm = "ktor sample app"
    val jwtSecret = "secret"
    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }*/
}
