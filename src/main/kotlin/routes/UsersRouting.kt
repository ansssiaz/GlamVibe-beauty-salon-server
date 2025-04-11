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
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.util.*

fun Route.usersRouting(repository: UserRepository, jwtService: JwtService) {
    route("/auth") {
        post("/registration") {
            try {
                val user = call.receive<RegisterUser>()
                val registeredUser = repository.registerUser(user)
                call.respond(HttpStatusCode.Created, registeredUser)
            } catch (ex: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest)
            } catch (ex: JsonConvertException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post("/login") {
            val logInUser = call.receive<LogInUser>()
            val user = repository.getUserByLogin(logInUser.login)
            if (user != null && logInUser.login == user.login && Bcrypt.verify(
                    logInUser.password,
                    user.passwordHash.decodeBase64Bytes()
                )
            ) {
                val tokenPair = jwtService.generateTokenPair(user)
                val expirationTime = jwtService.setExpirationTime()
                repository.updateRefreshToken(user.id, tokenPair.refreshToken, expirationTime)
                call.respond(tokenPair)
            } else
                call.respond(HttpStatusCode.Unauthorized)
        }

        post("/token-refresh") {
            val oldRefreshToken = call.receive<RefreshToken>()
            val currentTime = System.currentTimeMillis()
            val user = repository.getUserByToken(oldRefreshToken.refreshToken)

            if (user?.refreshToken != null && user.refreshTokenExpirationTime != null && user.refreshTokenExpirationTime > currentTime) {
                val tokenPair = jwtService.generateTokenPair(user)
                val expirationTime = jwtService.setExpirationTime()
                repository.updateRefreshToken(user.id, tokenPair.refreshToken, expirationTime)
                call.respond(tokenPair)
            } else
                call.respondText(
                    "Invalid token",
                    status = HttpStatusCode.BadRequest
                )
        }

        authenticate {
            post("/logout/{id}") {
                val id = call.parameters["id"]?.toInt()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                    return@post
                }
                val principal = call.principal<JWTPrincipal>()
                if (principal != null) {
                    val currentUserId = principal.payload.getClaim("id").asInt()
                    if (currentUserId == id) {
                        if (repository.logOut(id)) {
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
            get("/{id}") {
                val id = call.parameters["id"]?.toInt()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                val principal = call.principal<JWTPrincipal>()
                if (principal != null) {
                    val currentUserId = principal.payload.getClaim("id").asInt()
                    if (currentUserId == id) {
                        val user = repository.getUserById(id)
                        if (user == null) {
                            call.respond(HttpStatusCode.NotFound)
                            return@get
                        }
                        call.respond(user)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "Access denied")
                    }
                }
            }

            put("/{id}") {
                val id = call.parameters["id"] ?: return@put call.respondText(
                    "Missing id",
                    status = HttpStatusCode.BadRequest
                )
                repository.getUserById(id.toInt()) ?: return@put call.respondText(
                    "No client with id $id",
                    status = HttpStatusCode.NotFound
                )
                val newClient = call.receive<UpdateUser>()
                val updatedClient = repository.updateUserData(id.toInt(), newClient)
                call.respond(HttpStatusCode.OK, updatedClient)
            }
        }
    }

    route("/clients") {
        get {
            val clients = repository.getAllClients()
            call.respond(clients)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toInt()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val client = repository.getClientById(id)
            if (client == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respond(client)
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toInt()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            if (repository.deleteUser(id)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respondText(
                    "No client with id $id",
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }

    route("/administrators") {
        get {
            val administrators = repository.getAllAdministrators()
            call.respond(administrators)
        }
    }
}