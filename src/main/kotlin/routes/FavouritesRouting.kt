package com.glamvibe.routes

import com.glamvibe.domain.repository.FavouritesRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*

import io.ktor.server.routing.*

fun Route.favouritesRouting(repository: FavouritesRepository) {
    authenticate {
        route("/favourites") {
            get("/{clientId}") {
                try {
                    val baseUrl = "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}"
                    val clientId = call.parameters["clientId"]?.toInt()

                    if (clientId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }

                    val principal = call.principal<JWTPrincipal>()
                    if (principal != null) {
                        val currentUserId = principal.payload.getClaim("id").asInt()

                        if (currentUserId == clientId) {
                            val favourites = repository.getFavourites(clientId).map {
                                it.copy(
                                    imageUrl = "$baseUrl${it.imageUrl}"
                                )
                            }
                            call.respond(favourites)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Client is not found")
                }
            }

            post("/{clientId}/services/{serviceId}") {
                val serviceId = call.parameters["serviceId"]?.toInt()
                val clientId = call.parameters["clientId"]?.toInt()

                if (serviceId == null || clientId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                val principal = call.principal<JWTPrincipal>()

                if (principal != null) {
                    val currentUserId = principal.payload.getClaim("id").asInt()

                    if (currentUserId == clientId) {
                        if (repository.addToFavourites(clientId, serviceId)) {
                            call.respond(HttpStatusCode.Created)
                        } else {
                            call.respond(HttpStatusCode.NotFound)
                            return@post
                        }
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Access denied")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                }
            }

            delete("/{clientId}/services/{serviceId}") {
                try {
                    val serviceId = call.parameters["serviceId"]?.toInt()
                    val clientId = call.parameters["clientId"]?.toInt()

                    if (serviceId == null || clientId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@delete
                    }

                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserId = principal.payload.getClaim("id").asInt()

                        if (currentUserId == clientId) {
                            if (repository.deleteFromFavourites(clientId, serviceId)) {
                                call.respond(HttpStatusCode.OK)
                            } else {
                                call.respond(HttpStatusCode.InternalServerError)
                                return@delete
                            }
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Client or service are not found")
                }
            }
        }
    }
}