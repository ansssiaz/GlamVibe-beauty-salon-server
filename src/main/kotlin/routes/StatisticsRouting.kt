package com.glamvibe.routes

import com.glamvibe.domain.model.Role
import com.glamvibe.domain.model.getRoleByString
import com.glamvibe.domain.repository.StatisticsRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.statisticsRouting(repository: StatisticsRepository) {
authenticate {
    route("/statistics") {
        get("/{month}") { //статистика за месяц
            val principal = call.principal<JWTPrincipal>()

            if (principal != null) {
                val currentUserRole = principal.payload.getClaim("role").asString()

                if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                    val statistics = repository.getStatisticsByMonth()
                    call.respond(statistics)
                } else {
                    call.respond(HttpStatusCode.Forbidden, "Access denied")
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
            }
        }

        post("/{month}"){ //создать статистику за месяц
            val principal = call.principal<JWTPrincipal>()

            if (principal != null) {
                val currentUserRole = principal.payload.getClaim("role").asString()

                if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                    val statistics = repository.createStatisticsForMonth()
                    call.respond(statistics)
                } else {
                    call.respond(HttpStatusCode.Forbidden, "Access denied")
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
            }
        }
    }
}
}