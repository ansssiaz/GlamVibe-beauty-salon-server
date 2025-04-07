package com.glamvibe.routes

import com.glamvibe.domain.model.User
import com.glamvibe.domain.repository.UserRepository
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*

fun Route.usersRouting(repository: UserRepository) {
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
            val client = repository.getUserById(id)
            if (client == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respond(client)
        }

        post {
            try {
                val client = call.receive<User>()
                val addedClient = repository.addClient(client)
                call.respond(HttpStatusCode.Created, addedClient)
            } catch (ex: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest)
            } catch (ex: JsonConvertException) {
                call.respond(HttpStatusCode.BadRequest)
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
            val newClient = call.receive<User>()
            val updatedClient = repository.updateClient(id.toInt(), newClient)
            call.respond(HttpStatusCode.OK, updatedClient)
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