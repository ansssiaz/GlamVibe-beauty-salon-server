package com.glamvibe.plugins

import com.glamvibe.authentication.JwtService
import com.glamvibe.data.repository.*
import com.glamvibe.recommendations.RecommendationsService
import com.glamvibe.routes.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting(
    userRepository: UserRepositoryImpl,
    serviceRepository: ServiceRepositoryImpl,
    favouritesRepository: FavouritesRepositoryImpl,
    promotionRepository: PromotionRepositoryImpl,
    masterRepository: MasterRepositoryImpl,
    appointmentRepository: AppointmentRepositoryImpl,
    statisticsRepository: StatisticsRepositoryImpl,
    jwtService: JwtService,
    recommendationsService: RecommendationsService,
    recommendationsRepository: RecommendationsRepositoryImpl
) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        usersRouting(userRepository, recommendationsRepository, jwtService, recommendationsService)
        servicesRouting(serviceRepository)
        favouritesRouting(favouritesRepository)
        promotionsRouting(promotionRepository)
        mastersRouting(masterRepository)
        appointmentsRouting(appointmentRepository)
        statisticsRouting(statisticsRepository)
    }
}
