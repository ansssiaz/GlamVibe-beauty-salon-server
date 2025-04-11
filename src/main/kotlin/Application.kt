package com.glamvibe

import com.glamvibe.authentication.JwtConfig
import com.glamvibe.authentication.JwtService
import com.glamvibe.plugins.DatabaseConfiguration.configureDatabases
import com.glamvibe.data.repository.UserRepositoryImpl
import com.glamvibe.plugins.configureRouting
import com.glamvibe.plugins.configureSecurity
import com.glamvibe.plugins.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val issuer = "glamvibe-beauty-salon-server"
    val jwtSecret: String = System.getenv("JWT_SECRET")
    val accessLifetime = System.getenv("ACCESS_LIFETIME").toLong()
    val refreshLifetime = System.getenv("REFRESH_LIFETIME").toLong()

    val jwtConfig = JwtConfig(issuer, jwtSecret)
    val jwtService = JwtService(jwtConfig, accessLifetime, refreshLifetime)

    val userRepository = UserRepositoryImpl()

    configureSerialization()
    configureSecurity(jwtConfig)
    configureDatabases()
    configureRouting(userRepository, jwtService)
}
