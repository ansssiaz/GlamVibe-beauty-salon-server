package com.glamvibe.plugins

import com.glamvibe.data.repository.PostgresUserRepository
import com.glamvibe.routes.usersRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(userRepository: PostgresUserRepository) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        staticResources("/static", "static")
        usersRouting(userRepository)
    }
}
