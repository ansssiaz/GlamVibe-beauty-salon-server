package com.glamvibe.routes

import com.toxicbakery.bcrypt.Bcrypt
import com.glamvibe.domain.repository.UserRepository
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import com.glamvibe.authentication.JwtService
import com.glamvibe.data.model.request.LogInUser
import com.glamvibe.data.model.request.RegisterUser
import com.glamvibe.data.model.request.UpdateUser
import com.glamvibe.data.model.request.RefreshToken
import com.glamvibe.domain.model.Role
import com.glamvibe.domain.model.getRoleByString
import com.glamvibe.domain.repository.RecommendationsRepository
import com.glamvibe.recommendations.RecommendationsService
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.util.*

fun Route.usersRouting(
    userRepository: UserRepository,
    recommendationsRepository: RecommendationsRepository,
    jwtService: JwtService,
    recommendationsService: RecommendationsService
) {
    route("/auth") {
        post("/registration") {
            try {
                val user = call.receive<RegisterUser>()
                val registeredUser = userRepository.registerUser(user)

                if (registeredUser.form != null) {
                    recommendationsService.createRecommendationsByClientFormData(
                        requireNotNull(registeredUser.form)
                    )
                }

                call.respond(HttpStatusCode.Created, registeredUser)
            } catch (ex: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest)
            } catch (ex: JsonConvertException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post("/login") {
            val logInUser = call.receive<LogInUser>()
            val user = userRepository.getUserByLogin(logInUser.login)
            if (user != null && logInUser.login == user.login && Bcrypt.verify(
                    logInUser.password,
                    user.passwordHash.decodeBase64Bytes()
                )
            ) {
                val tokenPair = jwtService.generateTokenPair(user)
                val expirationTime = jwtService.setExpirationTime()
                userRepository.updateRefreshToken(user.id, tokenPair.refreshToken, expirationTime)
                call.respond(tokenPair)
            } else
                call.respond(HttpStatusCode.Unauthorized)
        }

        post("/token-refresh") {
            try {
                val oldRefreshToken = call.receive<RefreshToken>()
                val currentTime = System.currentTimeMillis()
                val user = userRepository.getUserByToken(oldRefreshToken.refreshToken)

                if (user?.refreshToken != null && user.refreshTokenExpirationTime != null && user.refreshTokenExpirationTime > currentTime) {
                    val tokenPair = jwtService.generateTokenPair(user)
                    val expirationTime = jwtService.setExpirationTime()
                    userRepository.updateRefreshToken(user.id, tokenPair.refreshToken, expirationTime)
                    call.respond(tokenPair)
                } else {
                    call.respondText(
                        "Invalid token",
                        status = HttpStatusCode.BadRequest
                    )
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.NotFound, e.message ?: "User is not found")
            }
        }

        authenticate {
            post("/logout/{id}") {
                val id = call.parameters["id"]?.toInt()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid user id")
                    return@post
                }
                val principal = call.principal<JWTPrincipal>()
                if (principal != null) {
                    val currentUserId = principal.payload.getClaim("id").asInt()
                    if (currentUserId == id) {
                        if (userRepository.logOut(id)) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respondText(
                                "No user with id $id",
                                status = HttpStatusCode.NotFound
                            )
                        }
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Access denied")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                }
            }
        }
    }

    authenticate {
        route("/me") {
            post {
                try {
                    val refreshToken = call.receive<RefreshToken>()
                    val currentTime = System.currentTimeMillis()
                    val user = userRepository.getUserByToken(refreshToken.refreshToken)

                    if (user?.refreshToken != null && user.refreshTokenExpirationTime != null && user.refreshTokenExpirationTime > currentTime) {
                        call.respond(user)
                    } else {
                        call.respondText(
                            "Invalid token",
                            status = HttpStatusCode.BadRequest
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "User is not found")
                }
            }

            get("/recommendations/{id}") {
                val id = call.parameters["id"]?.toInt()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val principal = call.principal<JWTPrincipal>()

                if (principal != null) {
                    val currentUserId = principal.payload.getClaim("id").asInt()

                    if (currentUserId == id) {
                        val baseUrl = "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}"
                        val recommendations = recommendationsRepository.getRecommendations(id)
                            .map {
                                it.copy(
                                    imageUrl = "$baseUrl${it.imageUrl}"
                                )
                            }
                        call.respond(recommendations)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Access denied")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toInt()
                    if (id == null) {
                        call.respondText(
                            "Missing id",
                            status = HttpStatusCode.BadRequest
                        )
                        return@put
                    }
                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserId = principal.payload.getClaim("id").asInt()

                        if (currentUserId == id) {
                            if (userRepository.getUserById(id) == null) {
                                call.respondText(
                                    "No user with id $id",
                                    status = HttpStatusCode.NotFound
                                )
                                return@put
                            }
                            val newUser = call.receive<UpdateUser>()
                            val updatedUser = userRepository.updateUserData(id, newUser)
                            call.respond(HttpStatusCode.OK, updatedUser)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "User is not found")
                }
            }
        }
    }

    authenticate {
        route("/clients") {
            get {
                val principal = call.principal<JWTPrincipal>()

                if (principal != null) {
                    val currentUserRole = principal.payload.getClaim("role").asString()

                    if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                        val clients = userRepository.getAllClients()
                        call.respond(clients)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Access denied")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                }
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toInt()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val principal = call.principal<JWTPrincipal>()

                if (principal != null) {
                    val currentUserRole = principal.payload.getClaim("role").asString()

                    if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                        val client = userRepository.getClientById(id)
                        if (client == null) {
                            call.respond(HttpStatusCode.NotFound)
                            return@get
                        }
                        call.respond(client)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Access denied")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                }
            }

            post {
                try {
                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserRole = principal.payload.getClaim("role").asString()

                        if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                            val user = call.receive<RegisterUser>()
                            val registeredUser = userRepository.registerUser(user)
                            call.respond(HttpStatusCode.Created, registeredUser)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (ex: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (ex: JsonConvertException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toInt()
                    if (id == null) {
                        call.respondText(
                            "Missing id",
                            status = HttpStatusCode.BadRequest
                        )
                        return@put
                    }
                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserRole = principal.payload.getClaim("role").asString()

                        if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                            if (userRepository.getUserById(id) == null) {
                                call.respondText(
                                    "No user with id $id",
                                    status = HttpStatusCode.NotFound
                                )
                                return@put
                            }
                            val newUser = call.receive<UpdateUser>()
                            val updatedUser = userRepository.updateUserData(id, newUser)
                            call.respond(HttpStatusCode.OK, updatedUser)
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

            delete("/{id}") {
                val id = call.parameters["id"]?.toInt()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }

                val principal = call.principal<JWTPrincipal>()

                if (principal != null) {
                    val currentUserRole = principal.payload.getClaim("role").asString()

                    if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                        if (userRepository.deleteUser(id)) {
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respondText(
                                "No client with id $id",
                                status = HttpStatusCode.NotFound
                            )
                        }
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