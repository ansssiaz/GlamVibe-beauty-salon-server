package com.glamvibe

import com.glamvibe.authentication.JwtConfig
import com.glamvibe.authentication.JwtService
import com.glamvibe.plugins.DatabaseConfiguration.configureDatabases
import com.glamvibe.data.repository.UserRepositoryImpl
import com.glamvibe.data.repository.ServiceRepositoryImpl
import com.glamvibe.data.repository.MasterRepositoryImpl
import com.glamvibe.data.repository.FavouritesRepositoryImpl
import com.glamvibe.data.repository.PromotionRepositoryImpl
import com.glamvibe.data.repository.StatisticsRepositoryImpl
import com.glamvibe.data.repository.AppointmentRepositoryImpl
import com.glamvibe.data.repository.RecommendationsRepositoryImpl
import com.glamvibe.recommendations.RecommendationsService
import com.glamvibe.plugins.configureRouting
import com.glamvibe.plugins.configureSecurity
import com.glamvibe.plugins.configureSerialization
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking

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
    val serviceRepository = ServiceRepositoryImpl()
    val favouritesRepository = FavouritesRepositoryImpl()
    val promotionsRepository = PromotionRepositoryImpl()
    val mastersRepository = MasterRepositoryImpl()
    val appointmentsRepository = AppointmentRepositoryImpl()
    val statisticsRepository = StatisticsRepositoryImpl()
    val recommendationsRepository = RecommendationsRepositoryImpl()

    val recommendationService = RecommendationsService(serviceRepository, recommendationsRepository)
    configureSerialization()
    configureSecurity(jwtConfig)
    configureDatabases()

    runBlocking {
        recommendationService.initialize()
    }

    configureRouting(
        userRepository,
        serviceRepository,
        favouritesRepository,
        promotionsRepository,
        mastersRepository,
        appointmentsRepository,
        statisticsRepository,
        jwtService,
        recommendationService,
        recommendationsRepository
    )
}
