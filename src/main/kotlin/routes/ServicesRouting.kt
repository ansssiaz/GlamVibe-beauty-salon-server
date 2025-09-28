package com.glamvibe.routes

import com.glamvibe.domain.model.Role
import com.glamvibe.domain.model.Service
import com.glamvibe.domain.model.getRoleByString
import com.glamvibe.domain.repository.ServiceRepository
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.io.File

fun Route.servicesRouting(repository: ServiceRepository) {
    get("/static/services/{filename}") {
        val filename = call.parameters["filename"] ?: throw NotFoundException()
        val mediaPath = System.getenv("SERVICES_MEDIA_PATH")
        val file = File("$mediaPath/$filename")
        if (file.exists()) {
            call.response.cacheControl(CacheControl.MaxAge(3600))
            call.respondFile(file)
        } else {
            throw NotFoundException("File not found")
        }
    }
    authenticate {
        route("/services") {
            get("/categories") {
                val categories = repository.getServicesCategories()
                call.respond(categories)
            }

            get {
                val baseUrl = "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}"
                val services = repository.getAllServices()
                    .map {
                        it.copy(
                            imageUrl = "$baseUrl${it.imageUrl}"
                        )
                    }

                call.respond(services)
            }

            get("/{id}") {
                val baseUrl = "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}"
                val id = call.parameters["id"]?.toInt()

                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val service = repository.getService(id)?.let {
                    it.copy(
                        imageUrl = "$baseUrl${it.imageUrl}"
                    )
                }

                if (service == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                call.respond(service)
            }

            get("/clients/{id}") {
                try {
                    val baseUrl = "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}"
                    val id = call.parameters["id"]?.toInt()

                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }

                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserId = principal.payload.getClaim("id").asInt()

                        if (currentUserId == id) {
                            val clientServices = repository.getAllServicesToClient(id)
                                .map {
                                    it.copy(
                                        imageUrl = "$baseUrl${it.imageUrl}"
                                    )
                                }

                            call.respond(clientServices)
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

            get("/{serviceId}/clients/{clientId}") {
                try {
                    val baseUrl = "${call.request.origin.scheme}://${call.request.host()}:${call.request.port()}"
                    val serviceId = call.parameters["serviceId"]?.toInt()
                    val clientId = call.parameters["clientId"]?.toInt()

                    if (serviceId == null || clientId == null) {
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }

                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserId = principal.payload.getClaim("id").asInt()

                        if (currentUserId == clientId) {
                            val clientService = repository.getServiceToClient(clientId, serviceId)?.let {
                                it.copy(
                                    imageUrl = "$baseUrl${it.imageUrl}"
                                )
                            }

                            if (clientService == null) {
                                call.respond(HttpStatusCode.NotFound)
                                return@get
                            }

                            call.respond(clientService)
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

            post {
                try {
                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserRole = principal.payload.getClaim("role").asString()

                        if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {
                            val multipartData = call.receiveMultipart()
                            var service: Service? = null
                            var imageUrl: String? = null

                            multipartData.forEachPart { part ->
                                when (part) {
                                    is PartData.FormItem -> {
                                        when (part.name) {
                                            "name" -> service =
                                                service?.copy(name = part.value) ?: Service(name = part.value)

                                            "category" -> service =
                                                service?.copy(category = part.value) ?: Service(
                                                    category = part.value
                                                )

                                            "description" -> service =
                                                service?.copy(description = part.value)
                                                    ?: Service(description = part.value)

                                            "duration" -> service = service?.copy(duration = part.value.toInt())
                                                ?: Service(duration = part.value.toInt())

                                            "price" -> service =
                                                service?.copy(price = part.value.toInt())
                                                    ?: Service(price = part.value.toInt())
                                        }
                                    }

                                    is PartData.FileItem -> {
                                        when (part.name) {
                                            "image" -> {
                                                val filename = part.originalFileName ?: return@forEachPart
                                                val mediaPath = System.getenv("SERVICES_MEDIA_PATH")
                                                val file = File("$mediaPath/$filename")
                                                part.provider().copyAndClose(file.writeChannel())
                                                imageUrl = "/static/services/$filename"
                                            }
                                        }
                                    }

                                    is PartData.BinaryChannelItem -> {}

                                    is PartData.BinaryItem -> {}
                                }
                                part.dispose()
                            }

                            if (service != null && imageUrl != null) {
                                val savedService =
                                    repository.addService(
                                        requireNotNull(service).copy(
                                            imageUrl = requireNotNull(
                                                imageUrl
                                            )
                                        )
                                    )
                                call.respond(HttpStatusCode.Created, savedService)
                            } else {
                                call.respond(HttpStatusCode.BadRequest, "Service data or image is missing.")
                            }
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Service category is not found")
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toInt() ?: return@put call.respondText(
                        "Missing id",
                        status = HttpStatusCode.BadRequest
                    )

                    val principal = call.principal<JWTPrincipal>()

                    if (principal != null) {
                        val currentUserRole = principal.payload.getClaim("role").asString()

                        if (currentUserRole.getRoleByString() == Role.ADMINISTRATOR) {

                            val existingService = repository.getService(id) ?: return@put call.respondText(
                                "No service with id $id",
                                status = HttpStatusCode.NotFound
                            )

                            val multipartData = call.receiveMultipart()
                            var service: Service? = null
                            var imageUrl: String? = null

                            multipartData.forEachPart { part ->
                                when (part) {
                                    is PartData.FormItem -> {
                                        when (part.name) {
                                            "name" -> service =
                                                service?.copy(name = part.value) ?: Service(name = part.value)

                                            "category" -> service =
                                                service?.copy(category = part.value) ?: Service(
                                                    category = part.value
                                                )

                                            "description" -> service =
                                                service?.copy(description = part.value)
                                                    ?: Service(description = part.value)

                                            "duration" -> service = service?.copy(duration = part.value.toInt())
                                                ?: Service(duration = part.value.toInt())

                                            "price" -> service =
                                                service?.copy(price = part.value.toInt())
                                                    ?: Service(price = part.value.toInt())
                                        }
                                    }

                                    is PartData.FileItem -> {
                                        when (part.name) {
                                            "image" -> {
                                                val filename = part.originalFileName
                                                if (filename != null) {
                                                    val mediaPath = System.getenv("SERVICES_MEDIA_PATH")
                                                    val file = File("$mediaPath/$filename")
                                                    part.provider().copyAndClose(file.writeChannel())
                                                    imageUrl = "/static/services/$filename"
                                                }
                                            }
                                        }
                                    }

                                    is PartData.BinaryChannelItem -> {}

                                    is PartData.BinaryItem -> {}
                                }
                                part.dispose()
                            }

                            if (service != null) {
                                val finalImageUrl = imageUrl ?: existingService.imageUrl
                                val updatedService =
                                    repository.updateService(
                                        id,
                                        requireNotNull(service).copy(imageUrl = finalImageUrl)
                                    )
                                call.respond(HttpStatusCode.OK, updatedService)
                            } else {
                                call.respond(HttpStatusCode.BadRequest, "Service data or image is missing.")
                            }
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Access denied")
                        }
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "User not authenticated")
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Service or service category are not found")
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
                        if (repository.deleteService(id)) {
                            //удалить фото с сервера
                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respondText(
                                "No service with id $id",
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